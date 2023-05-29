package kyo

import core._
import core.internal._
import ios._

object locals {

  trait Local[T] {

    import Locals._

    def default: T

    /*inline(3)*/
    def get: T > IOs =
      new KyoIO[T, Any] {
        def apply(v: Unit, s: Safepoint[IO, IOs], l: Locals.State) =
          l.getOrElse(Local.this, default).asInstanceOf[T]
      }

    def let[U, S1, S2](v: T > S1)(f: U > S2): U > (S1 & S2 & IOs) = {
      type M2[_]
      type E2 <: Effect[M2, E2]
      def loop(v: T, f: U > S2): U > S2 =
        f match {
          case kyo: Kyo[M2, E2, Any, U, S2] @unchecked =>
            new KyoCont[M2, E2, Any, U, S2](kyo) {
              def apply(v2: Any, s: Safepoint[M2, E2], l: Locals.State) =
                loop(v, kyo(v2, s, l.updated(Local.this, v)))
            }
          case _ =>
            f
        }
      v.map(loop(_, f))
    }
  }

  object Locals {

    type State = Map[Local[_], Any]

    object State {
      val empty: State = Map.empty
    }

    /*inline(3)*/
    def init[T](defaultValue: T): Local[T] =
      new Local[T] {
        def default = defaultValue
      }

    val save: State > IOs =
      new KyoIO[State, Any] {
        def apply(v: Unit, s: Safepoint[IO, IOs], l: Locals.State) =
          l
      }

    /*inline(3)*/
    def restore[T, S](st: State)(f: T > S): T > (IOs & S) =
      type M2[_]
      type E2 <: Effect[M2, E2]
      def loop(f: T > S): T > S =
        f match {
          case kyo: Kyo[M2, E2, Any, T, S] @unchecked =>
            new KyoCont[M2, E2, Any, T, S](kyo) {
              def apply(v2: Any, s: Safepoint[M2, E2], l: Locals.State) =
                loop(kyo(v2, s, l ++ st))
            }
          case _ =>
            f
        }
      loop(f)
  }
}
