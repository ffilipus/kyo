package kyo

import scala.runtime.AbstractFunction1

import core._
import core.internal._
import locals._
import ios._

object arrows {

  final class Arrows private[arrows] () extends Effect[[T] =>> Unit, Arrows] {
    /*inline(2)*/
    def apply[T, S, U, S2](
        f: T > (S & Arrows) => U > (S2 & Arrows)
    ): T > S => U > (S2 & IOs) =
      new AbstractFunction1[T > S, U > (S2 & IOs)] {
        val a =
          new Kyo[[T] =>> Unit, Arrows, T > S, T, S & Arrows] {
            def value  = ()
            def effect = arrows.Arrows
            def apply(v: T > S, s: Safepoint[[T] =>> Unit, Arrows], l: Locals.State) =
              v
          }
        def apply(v: T > S) =
          Locals.save.map { st =>
            f(a).asInstanceOf[Kyo[[T] =>> Unit, Arrows, T > S, U, S2]](
                v,
                Safepoint.noop,
                st
            )
          }
      }

    /*inline(2)*/
    def recursive[T, S, U, S2](f: (
        T > (S & Arrows),
        T > (S & Arrows) => U > (S2 & Arrows)
    ) => U > (S2 & Arrows)): T > S => U > (S2 & IOs) =
      new AbstractFunction1[T > S, U > (S2 & IOs)] {
        val a =
          new Kyo[[T] =>> Unit, Arrows, T > S, T, S & Arrows] {
            def value  = ()
            def effect = arrows.Arrows
            def apply(v: T > S, s: Safepoint[[T] =>> Unit, Arrows], l: Locals.State) =
              v
          }
        val g = f(a, this.asInstanceOf[T > (S & Arrows) => U > (S2 & Arrows)])
          .asInstanceOf[Kyo[[T] =>> Unit, Arrows, T > S, U, S2]]
        def apply(v: T > S) =
          Locals.save.map { st =>
            g(v, Safepoint.noop, st)
          }
      }
  }
  val Arrows = new Arrows
}
