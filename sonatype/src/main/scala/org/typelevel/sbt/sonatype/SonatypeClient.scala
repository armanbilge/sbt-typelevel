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

import cats.effect.Concurrent
import cats.syntax.all._
import org.http4s.client.Client
import org.http4s.Uri
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.circe.CirceEntityCodec._
import org.http4s.Method._
import fs2.io.file.Path
import org.http4s.Credentials
import org.http4s.headers.Authorization
import org.http4s.syntax.all._

private[sbt] trait SonatypeClient[F[_]] {

  /**
   * @see
   *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/path__staging_profile_repositories_-profileIdKey-.html]]
   */
  def getProfileRepositories(profileId: String): F[StagingRepositories]

  /**
   * Entry point to trigger staging repository creation.
   *
   * @see
   *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/path__staging_profiles_-profileIdKey-_start.html]]
   */
  def startStagingRepository(profileId: String, promote: StagingPromote): F[StagingPromote]

  /**
   * Entry point to trigger staging repository drop (remove repository and its contents without
   * promoting it).
   *
   * @see
   *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/path__staging_profiles_-profileIdKey-_drop.html]]
   */
  def dropStagingRepository(profileId: String, promote: StagingPromote): F[Unit]

  /**
   * Entry point to trigger staging repository close (finish deploy and close staging
   * repository).
   *
   * @see
   *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/path__staging_profiles_-profileIdKey-_finish.html]]
   */
  def finishStagingRepository(profileId: String, promote: StagingPromote): F[Unit]

  /**
   * Entry point to trigger staging repository promotion.
   *
   * @see
   *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/path__staging_profiles_-profileIdKey-_promote.html]]
   */
  def promoteStagingRepository(profileId: String, promote: StagingPromote): F[Unit]

  /**
   * Handles the uploaded bundle files (multiple of them).
   *
   * @see
   *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/path__staging_bundle_upload.html]]
   */
  def uploadBundle(path: Path): F[Unit] = ???

}

private[sbt] object SonatypeClient {

  def apply[F[_]: Concurrent](
      client: Client[F],
      repo: Uri,
      credentials: Credentials): SonatypeClient[F] = {
    val base = repo / "service" / "local" / "staging"
    impl(
      Client { req =>
        client.run(req.withUri(base.resolve(req.uri)).withHeaders(Authorization(credentials)))
      }
    )
  }

  private def impl[F[_]: Concurrent](client: Client[F]): SonatypeClient[F] =
    new SonatypeClient[F] with Http4sClientDsl[F] {

      def getProfileRepositories(profileId: String): F[StagingRepositories] =
        client.expect(uri"profile_repositories" / profileId)

      def startStagingRepository(
          profileId: String,
          promote: StagingPromote): F[StagingPromote] =
        client
          .expect[PromoteResponse](
            POST(PromoteRequest(promote), uri"profiles" / profileId / "start"))
          .map(_.data)

      def dropStagingRepository(profileId: String, promote: StagingPromote): F[Unit] =
        client.expect(POST(PromoteRequest(promote), uri"profiles" / profileId / "drop"))

      def finishStagingRepository(profileId: String, promote: StagingPromote): F[Unit] =
        client.expect(POST(PromoteRequest(promote), uri"profiles" / profileId / "finish"))

      def promoteStagingRepository(profileId: String, promote: StagingPromote): F[Unit] =
        client.expect(POST(PromoteRequest(promote), uri"profiles" / profileId / "promote"))

    }

}
