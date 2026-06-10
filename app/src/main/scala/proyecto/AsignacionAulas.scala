package proyecto

import scala.util.Random

object AsignacionAulas {

  type Curso      = (String, Int, Int, Int)
  type Cursos     = Vector[Curso]
  type Aula       = (String, Int)
  type Aulas      = Vector[Aula]
  type Asignacion = Vector[Int]
  type Distancias = Vector[Vector[Int]]
  type Pesos      = (Int, Int, Int, Int)

  // Generación (NO MODIFICAR)

  val random = new Random()

  def cursosAlAzar(n: Int): Cursos =
    Vector.tabulate(n) { i =>
      val ini = random.nextInt(29)
      val dur = random.nextInt(7) + 2
      ("C" + i, ini, ini + dur, random.nextInt(46) + 5)
    }

  def aulasAlAzar(m: Int): Aulas =
    Vector.tabulate(m)(j => ("E" + j, random.nextInt(46) + 15))

  def distanciasAlAzar(m: Int): Distancias = {
    val v = Vector.fill(m, m)(random.nextInt(m * 2) + 1)
    Vector.tabulate(m, m)((i, j) =>
      if (i < j) v(i)(j)
      else if (i == j) 0
      else v(j)(i))
  }

  // Accesores (NO MODIFICAR)

  def idCurso(c: Curso): String = c._1
  def iniCurso(c: Curso): Int   = c._2
  def finCurso(c: Curso): Int   = c._3
  def estCurso(c: Curso): Int   = c._4

  def idAula(a: Aula): String = a._1
  def capAula(a: Aula): Int   = a._2

  // Implementaciones (versión de tu compañero con recursión explícita)


  def solapan(c1: Curso, c2: Curso): Boolean =
    iniCurso(c1) < finCurso(c2) && iniCurso(c2) < finCurso(c1)

  def choques(cursos: Cursos, a: Asignacion): Int = {
    val n = cursos.length
    def choquesConI(i: Int, j: Int): Int = {
      if (j >= n) 0
      else {
        val hayChoque = a(i) >= 0 && a(i) == a(j) && solapan(cursos(i), cursos(j))
        (if (hayChoque) 1 else 0) + choquesConI(i, j + 1)
      }
    }
    def recorre(i: Int): Int = {
      if (i >= n - 1) 0
      else choquesConI(i, i + 1) + recorre(i + 1)
    }
    recorre(0)
  }

  def capacidadFallida(cursos: Cursos, aulas: Aulas, a: Asignacion): Int =
    cursos.indices.foldLeft(0) { (acc, i) =>
      if (a(i) >= 0 && capAula(aulas(a(i))) < estCurso(cursos(i))) acc + 1
      else acc
    }

  def desperdicio(cursos: Cursos, aulas: Aulas, a: Asignacion): Int =
    cursos.indices.foldLeft(0) { (acc, i) =>
      if (a(i) >= 0) {
        val diff = capAula(aulas(a(i))) - estCurso(cursos(i))
        if (diff >= 0) acc + diff else acc
      } else acc
    }

  def movilidad(cursos: Cursos, aulas: Aulas, d: Distancias,
                a: Asignacion): Int = {
    val asignados = cursos.indices
      .filter(i => a(i) >= 0)
      .sortBy(i => iniCurso(cursos(i)))

    def sumaDistancias(idx: Int, acc: Int): Int = {
      if (idx >= asignados.length - 1) acc
      else {
        val aulaActual    = a(asignados(idx))
        val aulaSiguiente = a(asignados(idx + 1))
        sumaDistancias(idx + 1, acc + d(aulaActual)(aulaSiguiente))
      }
    }
    sumaDistancias(0, 0)
  }

  def costoAsignacion(cursos: Cursos, aulas: Aulas, d: Distancias,
                      a: Asignacion, w: Pesos): Int = {
    val ch = choques(cursos, a)
    val cf = capacidadFallida(cursos, aulas, a)
    val de = desperdicio(cursos, aulas, a)
    val mv = movilidad(cursos, aulas, d, a)
    w._1 * ch + w._2 * cf + w._3 * de + w._4 * mv
  }

  def generarAsignaciones(n: Int, m: Int): Vector[Asignacion] = {
    if (n == 0) Vector(Vector.empty[Int])
    else {
      val subAsignaciones = generarAsignaciones(n - 1, m)
      (0 until m).toVector.flatMap { j =>
        subAsignaciones.map(sub => j +: sub)
      }
    }
  }

  def asignacionOptima(cursos: Cursos, aulas: Aulas, d: Distancias,
                       w: Pesos): (Asignacion, Int) = {
    val candidatas = generarAsignaciones(cursos.length, aulas.length)
    def buscarMinimo(idx: Int, mejorAsig: Asignacion, mejorCosto: Int): (Asignacion, Int) = {
      if (idx >= candidatas.length) (mejorAsig, mejorCosto)
      else {
        val costo = costoAsignacion(cursos, aulas, d, candidatas(idx), w)
        if (costo < mejorCosto) buscarMinimo(idx + 1, candidatas(idx), costo)
        else                    buscarMinimo(idx + 1, mejorAsig, mejorCosto)
      }
    }
    val primerCosto = costoAsignacion(cursos, aulas, d, candidatas(0), w)
    buscarMinimo(1, candidatas(0), primerCosto)
  }
}