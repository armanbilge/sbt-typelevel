/*
 * Copyright 2022 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.sbt.sonatype

import fs2.io.file.Files
import fs2.io.file.Path
import cats.effect.Concurrent
import cats.syntax.all._
import org.http4s.Uri

private[sbt] trait SonatypeService[F[_]] {

  /**
   * @see
   *   [[https://support.sonatype.com/hc/en-us/articles/213465868-Uploading-to-a-Staging-Repository-via-REST-API]]
   */
  def releaseBundle(session: String, bundle: Path): F[Unit]

}

private[sbt] object SonatypeService {

  def apply[F[_]: Files](client: SonatypeClient[F], profileName: String)(
      implicit F: Concurrent[F]): SonatypeService[F] = new SonatypeService[F] {

    def releaseBundle(session: String, bundle: Path): F[Unit] = {
      for {
        profile <- stagingProfile
        _ <- dropIfExists(profile, session)
        promote <- startStaging(profile, session)
        _ <- uploadBundle(promote, bundle)
      } yield ()
    }

    def stagingProfile: F[StagingProfile] =
      client.getProfiles.flatMap { profiles =>
        profiles.find(_.name.contains(profileName)) match {
          case Some(profile) => profile.pure
          case None =>
            F.raiseError(
              new NoSuchElementException(s"Could not find profile for ${profileName}"))
        }
      }

    def dropIfExists(profile: StagingProfile, session: String): F[Unit] =
      client.getProfileRepositories(profile.id).flatMap { repos =>
        repos.traverse_ {
          case StagingProfileRepository(`session`, Some(repositoryId)) =>
            client.dropStagingRepository(
              profile.id,
              StagingPromote(
                stagedRepositoryId = Some(repositoryId),
                targetRepositoryId = Some(profile.repositoryTargetId),
                description = session
              )
            )
          case _ => F.unit
        }
      }

    def startStaging(profile: StagingProfile, session: String): F[StagingPromote] =
      client.startStagingRepository(
        profile.id,
        StagingPromote(None, description = session, None)
      )

    def uploadBundle(promote: StagingPromote, bundle: Path): F[Unit] =
      Files[F]
        .walk(bundle)
        .evalFilter(Files[F].isRegularFile(_))
        .parEvalMapUnordered(Int.MaxValue) { path =>
          client.deployByRepositoryId(
            promote.stagedRepositoryId.get,
            Uri.Path.unsafeFromString(path.toString))(path)
        }
        .compile
        .drain

  }

}
