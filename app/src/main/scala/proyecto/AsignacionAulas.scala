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

  /**
   * Determina si dos cursos se traslapan en el tiempo.
   *
   * Dos cursos se consideran solapados cuando existe al menos
   * un intervalo de tiempo en común entre ellos.
   *
   * Esta función es la base para detectar choques de horario
   * cuando dos cursos son asignados a la misma aula.
   */
  def solapan(c1: Curso, c2: Curso): Boolean =
    iniCurso(c1) < finCurso(c2) && iniCurso(c2) < finCurso(c1)

  /**
   * Calcula la cantidad total de choques de horario.
   *
   * Un choque ocurre cuando dos cursos:
   * - Están asignados a la misma aula.
   * - Tienen horarios que se traslapan.
   *
   * La función recorre todas las parejas posibles de cursos
   * utilizando recursión para contar los choques encontrados.
   */
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

  /**
   * Calcula cuántos cursos fueron asignados a aulas cuya capacidad
   * es insuficiente para la cantidad de estudiantes inscritos.
   *
   * Cada vez que la capacidad del aula es menor que la cantidad
   * de estudiantes del curso, se incrementa el contador.
   */
  def capacidadFallida(cursos: Cursos, aulas: Aulas, a: Asignacion): Int =
    cursos.indices.foldLeft(0) { (acc, i) =>
      if (a(i) >= 0 && capAula(aulas(a(i))) < estCurso(cursos(i))) acc + 1
      else acc
    }

  /**
   * Calcula el desperdicio total de capacidad.
   *
   * El desperdicio corresponde a los puestos vacíos que quedan
   * cuando un aula tiene más capacidad que estudiantes.
   *
   * Se suma el exceso de capacidad de cada curso asignado.
   */
  def desperdicio(cursos: Cursos, aulas: Aulas, a: Asignacion): Int =
    cursos.indices.foldLeft(0) { (acc, i) =>
      if (a(i) >= 0) {
        val diff = capAula(aulas(a(i))) - estCurso(cursos(i))
        if (diff >= 0) acc + diff else acc
      } else acc
    }

  /**
   * Calcula el costo de movilidad entre aulas.
   *
   * Primero ordena los cursos según su hora de inicio.
   * Luego suma las distancias recorridas entre las aulas
   * de cursos consecutivos.
   *
   * Mientras mayor sea la distancia entre aulas,
   * mayor será el costo de movilidad.
   */
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

  /**
   * Calcula el costo total de una asignación.
   *
   * El costo se obtiene combinando cuatro criterios:
   * - Choques de horario.
   * - Capacidad insuficiente.
   * - Desperdicio de capacidad.
   * - Movilidad entre aulas.
   *
   * Cada criterio se multiplica por un peso definido
   * para reflejar su importancia dentro del problema.
   */
  def costoAsignacion(cursos: Cursos, aulas: Aulas, d: Distancias,
                      a: Asignacion, w: Pesos): Int = {
    val ch = choques(cursos, a)
    val cf = capacidadFallida(cursos, aulas, a)
    val de = desperdicio(cursos, aulas, a)
    val mv = movilidad(cursos, aulas, d, a)
    w._1 * ch + w._2 * cf + w._3 * de + w._4 * mv
  }

  /**
   * Genera todas las asignaciones posibles de aulas para los cursos.
   *
   * Utiliza recursión para construir todas las combinaciones.
   *
   * Cada posición del vector representa un curso y el valor
   * almacenado corresponde al aula asignada.
   *
   * El número total de asignaciones generadas es m**n,
   * donde n es la cantidad de cursos y m la cantidad de aulas.
   */
  def generarAsignaciones(n: Int, m: Int): Vector[Asignacion] = {
    if (n == 0) Vector(Vector.empty[Int])
    else {
      val subAsignaciones = generarAsignaciones(n - 1, m)
      (0 until m).toVector.flatMap { j =>
        subAsignaciones.map(sub => j +: sub)
      }
    }
  }

  /**
   * Busca la asignación con el menor costo posible.
   *
   * Primero genera todas las asignaciones candidatas.
   * Después evalúa cada una utilizando la función de costo.
   *
   * Finalmente conserva la asignación que produzca
   * el menor costo total y la devuelve junto con dicho costo.
   *
   * Esta función garantiza encontrar la solución óptima
   * porque analiza todas las posibilidades existentes.
   */
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