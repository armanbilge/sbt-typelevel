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

package org.typelevel.sbt.mergify

import sbt._, Keys._
import org.typelevel.sbt.gha._

object MergifyPlugin extends AutoPlugin {

  object autoImport {
    lazy val mergifyPrRules = settingKey[Seq[MergifyPrRule]]("The mergify pull request rules")

    lazy val mergifyStewardConfig = settingKey[Option[MergifyStewardConfig]](
      "Config for the automerge rule for Scala Steward PRs, set to None to disable.")

    lazy val mergifyRequiredJobs =
      settingKey[Seq[String]]("Ids for jobs that must succeed for merging (default: [build])")

    lazy val mergifySuccessConditions = settingKey[Seq[MergifyCondition]](
      "Success conditions for merging (default: auto-generated from `mergifyRequiredJobs` setting)")
  }

  override def requires = GenerativePlugin
  override def trigger: PluginTrigger = allRequirements

  import autoImport._
  import GenerativePlugin.autoImport._

  override def buildSettings: Seq[Setting[_]] = Seq(
    mergifyRequiredJobs := Seq("build"),
    mergifySuccessConditions := jobSuccessConditions.value,
    mergifyPrRules := {
      mergifyStewardConfig.value.map(_.toPrRule(mergifySuccessConditions.value.toList)).toList
    }
  )

  private lazy val jobSuccessConditions = Def.setting {
    githubWorkflowGeneratedCI.value.flatMap {
      case job if mergifyRequiredJobs.value.contains(job.id) =>
        GenerativePlugin
          .expandMatrix(
            job.oses,
            job.scalas,
            job.javas,
            job.matrixAdds,
            job.matrixIncs,
            job.matrixExcs
          )
          .map { cell =>
            MergifyCondition.Custom(s"status-success=${job.name} (${cell.mkString(", ")})")
          }
      case _ => Nil
    }
  }

}