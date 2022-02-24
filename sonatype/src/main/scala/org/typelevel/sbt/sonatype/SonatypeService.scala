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

import fs2.io.file.Path
import cats.MonadThrow
import cats.syntax.all._

private[sbt] trait SonatypeService[F[_]] {

  /**
   * @see
   *   [[https://support.sonatype.com/hc/en-us/articles/213465868-Uploading-to-a-Staging-Repository-via-REST-API]]
   */
  def releaseBundle(session: String, bundle: Path): F[Unit]

}

private[sbt] object SonatypeService {

  def apply[F[_]](client: SonatypeClient[F], profileName: String)(
      implicit F: MonadThrow[F]): SonatypeService[F] = new SonatypeService[F] {

    def releaseBundle(session: String, bundle: Path): F[Unit] = {
      for {
        profile <- stagingProfile
        _ <- dropIfExists(profile, session)
        _ <- client.startStagingRepository(profile.id, )
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
          case repo if repo.description.contains(session) =>
            client.dropStagingRepository(
              profile.id,
              StagingPromote(
                stagedRepositoryId = repo.repositoryId,
                targetRepositoryId = profile.repositoryTargetId,
                description = repo.description
              )
            )
          case _ => F.unit
        }
      }

      def startStaging(profile: StagingProfile, session: String): F[Unit] =
        client.startStagingRepository(
          profile.id,
          StagingPromote(
            
          )
        )

  }

}
