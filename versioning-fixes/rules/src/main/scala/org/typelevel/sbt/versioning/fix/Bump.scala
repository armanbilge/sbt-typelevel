/*
 * Copyright 2024 Typelevel
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

package org.typelevel.sbt.versioning.fix

import scala.meta._
import scalafix.v1._

abstract class Bump(kind: String) extends SyntacticRule(s"SbtTypelevelVersioning${kind}Bump") {

  override def fix(implicit doc: SyntacticDocument): Patch =
    doc
      .tree
      .collect {
        case Term
              .ApplyInfix
              .After_4_6_0(
                Term.Name("tlBaseVersion"),
                Term.Name(":="),
                _,
                List(Term.ArgClause(List(bv @ Lit.String(BaseVersion(oldMajor, oldMinor))), _))
              ) =>
          val (newMajor, newMinor) = bump(oldMajor.toLong, oldMinor.toLong)
          Patch.replaceTree(bv, s"$newMajor.$newMinor")
      }
      .asPatch

  protected def bump(major: Long, minor: Long): (Long, Long)

  private val BaseVersion = """(\d+)\.(\d+)""".r

}

class MajorBump extends Bump("Major") {

  protected def bump(major: Long, minor: Long): (Long, Long) = (major + 1, 0)

}

class MinorBump extends Bump("Minor") {

  protected def bump(major: Long, minor: Long): (Long, Long) = (major, minor + 1)

}
