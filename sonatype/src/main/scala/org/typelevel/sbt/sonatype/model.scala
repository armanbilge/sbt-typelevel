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

import io.circe.Decoder
import io.circe.Encoder

/**
 * @see
 *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/el_ns0_stagingProfiles.html]]
 */
private[sbt] final case class StagingProfiles(data: List[StagingProfile])

private[sbt] object StagingProfiles {
  implicit def decoder: Decoder[StagingProfiles] =
    Decoder.forProduct1("data")(StagingProfiles(_))
}

/**
 * @see
 *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/ns0_stagingProfile.html]]
 */
private[sbt] final case class StagingProfile(
    id: String,
    name: String,
    repositoryTargetId: String
)

private[sbt] object StagingProfile {
  implicit def decoder: Decoder[StagingProfile] =
    Decoder.forProduct3("id", "name", "repositoryTargetId")(StagingProfile(_, _, _))
}

/**
 * @see
 *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/el_ns0_stagingRepositories.html]]
 */
private[sbt] final case class StagingRepositories(data: List[StagingProfileRepository])

private[sbt] object StagingRepositories {
  implicit def decoder: Decoder[StagingRepositories] =
    Decoder.forProduct1("data")(StagingRepositories(_))
}

/**
 * @see
 *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/ns0_stagingProfileRepository.html]]
 */
private[sbt] final case class StagingProfileRepository(
    description: String,
    repositoryId: Option[String]
)

private[sbt] object StagingProfileRepository {
  implicit def decoder: Decoder[StagingProfileRepository] =
    Decoder.forProduct2("description", "repositoryId")(StagingProfileRepository(_, _))
}

/**
 * @see
 *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/el_ns0_promoteRequest.html]]
 */
private[sbt] final case class PromoteRequest(data: StagingPromote)

private[sbt] object PromoteRequest {
  implicit def encoder: Encoder[PromoteRequest] = Encoder.forProduct1("data")(_.data)
}

/**
 * @see
 *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/el_ns0_promoteRequest.html]]
 */
private[sbt] final case class PromoteResponse(data: StagingPromote)

private[sbt] object PromoteResponse {
  implicit def decoder: Decoder[PromoteResponse] =
    Decoder.forProduct1("data")(PromoteResponse(_))
}

/**
 * @see
 *   [[https://repository.sonatype.org/nexus-staging-plugin/default/docs/ns0_stagingPromote.html]]
 */
private[sbt] final case class StagingPromote(
    stagedRepositoryId: Option[String],
    description: String,
    targetRepositoryId: Option[String]
)

private[sbt] object StagingPromote {
  implicit def decoder: Decoder[StagingPromote] =
    Decoder.forProduct3("stagedRepositoryId", "description", "targetRepositoryId")(
      StagingPromote(_, _, _))

  implicit def encoder: Encoder[StagingPromote] =
    Encoder.forProduct3("stagedRepositoryId", "description", "targetRepositoryId") { x =>
      (x.stagedRepositoryId, x.description, x.targetRepositoryId)
    }
}

private[sbt] final case class StagingActivity(
    name: String,
    events: List[ActivityEvent]
)

private[sbt] object StagingActivity {
  implicit def decoder: Decoder[StagingActivity] =
    Decoder.forProduct2("name", "events")(StagingActivity(_, _))
}

private[sbt] final case class ActivityEvent(
    name: String
)

private[sbt] object ActivityEvent {
  implicit def decoder: Decoder[ActivityEvent] = Decoder.forProduct1("name")(ActivityEvent(_))
}
