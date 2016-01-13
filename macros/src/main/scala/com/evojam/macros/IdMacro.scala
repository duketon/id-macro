package com.evojam.macros

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object idMacro {
  def impl(c: blackbox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def modifiedClassDef(classDec: ClassDef) = {
      try {
        val q"case class $name(..$fields) extends ..$parents { ..$body }" = classDec
        fields.length match {
          case 1 => q"""
            case class $name(${fields.head}) extends ..$parents {
              require(${fields.head.name} != null)
              ..$body
            }
          """
          case _ => c.abort(c.enclosingPosition, "Annotation is only supported on case class with one field")
        }
      } catch {
        case _:MatchError => c.abort(c.enclosingPosition, "Annotation is only supported on case class")
      }
    }

    def modifiedModuleDef(moduleDec: Option[ModuleDef], className: TypeName) = {
      val unapply = q"def unapply(in: ${typeOf[String]}) = Option(in).filter(_.nonEmpty).map(apply)"
      moduleDec.map { module =>
        val q"object $name extends ..$parents { ..$body }" = module
        q"""
           object $name extends ..$parents {
              ..$body
              $unapply
           }
         """
      }.getOrElse {
        q"""
            object ${className.toTermName} {
              $unapply
            }
          """
        }
      }

    def modifiedDec(classDef: ClassDef, moduleDef: Option[ModuleDef]) =
      c.Expr[Any](q"""
           ${modifiedClassDef(classDef)}
           ${modifiedModuleDef(moduleDef, classDef.name)}
        """)

    annottees.map(_.tree) match {
      case (classDecl: ClassDef) :: Nil => modifiedDec(classDecl, None)
      case (classDecl: ClassDef) :: (moduleDef: ModuleDef) :: Nil => modifiedDec(classDecl, Some(moduleDef))
      case _ => c.abort(c.enclosingPosition, "Invalid annottee")
    }

  }
}

@compileTimeOnly("enable macro paradise to expand macro annotations")
class id extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro idMacro.impl
}