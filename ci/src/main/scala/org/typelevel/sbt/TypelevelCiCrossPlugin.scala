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

package org.typelevel.sbt

import sbt._, Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin

object TypelevelCiCrossPlugin extends AutoPlugin {

  override def trigger = noTrigger

  import ScalaJSPlugin.autoImport._

  override def derivedProjects(proj: ProjectDefinition[_]) = {

    val projects = proj.aggregate.map(_.asInstanceOf[ProjectReference])
    val jsProjects = projects
      .map(p => (p / scalaJSUseMainModuleInitializer).?.value)

    Seq(
      Project("rootJVM", file(".jvm"))
        .aggregate(proj.aggregate.map(_.asInstanceOf[ProjectReference]): _*)
      // Project("rootJS", file(".js"))
    )
  }

}
