package proyecto

import common._
import AsignacionAulas._

object AsignacionAulasPar {

  /**
   * Esta función calcula los choques de horario utilizando paralelismo.
   *
   * La idea es dividir el conjunto de cursos en dos partes y procesarlas
   * al mismo tiempo usando parallel.
   *
   * Un choque ocurre cuando:
   * - Dos cursos están asignados a la misma aula.
   * - Sus horarios se traslapan.
   *
   * Finalmente se suman los choques encontrados en cada mitad.
   */
  def choquesPar(cursos: Cursos, a: Asignacion): Int = {
    val n = cursos.length

    // Cuenta los choques del curso en índice i con todos los j en [jIni, jFin)
    def choquesConRango(i: Int, jIni: Int, jFin: Int): Int = {
      if (jIni >= jFin) 0
      else {
        val hayChoque = a(i) >= 0 && a(i) == a(jIni) && solapan(cursos(i), cursos(jIni))
        (if (hayChoque) 1 else 0) + choquesConRango(i, jIni + 1, jFin)
      }
    }

    // Divide el rango [iIni, iFin) de "primeros índices" en paralelo
    def aux(iIni: Int, iFin: Int): Int = {
      if (iFin - iIni <= 1) {
        // caso base: solo el índice iIni, compara con todos los j > iIni
        choquesConRango(iIni, iIni + 1, n)
      } else {
        val mid = iIni + (iFin - iIni) / 2
        val (izq, der) = parallel(aux(iIni, mid), aux(mid, iFin))
        izq + der
      }
    }

    if (n < 2) 0 else aux(0, n - 1)
  }

  /**
   * Esta función calcula el desperdicio total de capacidad usando paralelismo.
   *
   * El desperdicio corresponde a los puestos vacíos que quedan en un aula
   * cuando la capacidad es mayor que la cantidad de estudiantes.
   *
   * El vector de cursos se divide en dos mitades y cada una se procesa
   * simultáneamente para acelerar el cálculo.
   */
  def desperdicioPar(cursos: Cursos, aulas: Aulas, a: Asignacion): Int = {
    def aux(ini: Int, fin: Int): Int = {
      if (fin - ini <= 1) {
        // caso base: un solo curso
        if (a(ini) >= 0) {
          val diff = capAula(aulas(a(ini))) - estCurso(cursos(ini))
          if (diff >= 0) diff else 0
        } else 0
      } else {
        val mid = ini + (fin - ini) / 2
        val (izq, der) = parallel(aux(ini, mid), aux(mid, fin))
        izq + der
      }
    }
    if (cursos.isEmpty) 0 else aux(0, cursos.length)
  }

  /**
   * Esta función calcula el costo de movilidad utilizando paralelismo.
   *
   * Primero ordena los cursos según su hora de inicio.
   * Después calcula la distancia que habría que recorrer entre
   * las aulas de cursos consecutivos.
   *
   * La suma de distancias se divide en dos partes para ejecutarse
   * simultáneamente y reducir el tiempo de cálculo.
   */
  def movilidadPar(cursos: Cursos, aulas: Aulas, d: Distancias,
                   a: Asignacion): Int = {
    val asignados = cursos.indices
      .filter(i => a(i) >= 0)
      .sortBy(i => iniCurso(cursos(i)))
      .toVector

    def aux(ini: Int, fin: Int): Int = {
      if (fin - ini <= 1) 0
      else if (fin - ini == 2) d(a(asignados(ini)))(a(asignados(ini + 1)))
      else {
        val mid = ini + (fin - ini) / 2
        // izq suma distancias internas de [ini, mid)
        // der suma distancias internas de [mid, fin)
        // más la distancia de unión entre el último de izq y el primero de der
        val (izq, der) = parallel(aux(ini, mid), aux(mid, fin))
        izq + d(a(asignados(mid - 1)))(a(asignados(mid))) + der
      }
    }
    if (asignados.length < 2) 0 else aux(0, asignados.length)
  }


  /**
   * Esta función genera todas las asignaciones posibles usando paralelismo.
   *
   * Cada posición del vector representa el aula asignada a un curso.
   *
   * La estrategia consiste en dividir los posibles valores del primer curso
   * y generar las combinaciones restantes en paralelo.
   */
  def generarAsignacionesPar(n: Int, m: Int): Vector[Asignacion] = {
    if (n == 0) Vector(Vector.empty[Int])
    else {
      // Divide los valores del primer índice en dos mitades y las procesa en paralelo
      def aux(vals: Vector[Int]): Vector[Asignacion] = {
        if (vals.length <= 1) {
          val sub = generarAsignaciones(n - 1, m)
          sub.map(s => vals(0) +: s)
        } else {
          val mid = vals.length / 2
          val (izq, der) = parallel(aux(vals.slice(0, mid)), aux(vals.slice(mid, vals.length)))
          izq ++ der
        }
      }
      aux((0 until m).toVector)
    }
  }

  /**
   * Esta función busca la asignación con menor costo utilizando paralelismo.
   *
   * Primero genera todas las asignaciones candidatas.
   * Luego divide la búsqueda del mínimo en dos partes y las evalúa
   * simultáneamente.
   *
   * Al final se conserva la asignación que tenga el menor costo.
   */
  def asignacionOptimaPar(cursos: Cursos, aulas: Aulas, d: Distancias,
                          w: Pesos): (Asignacion, Int) = {
    val candidatas = generarAsignacionesPar(cursos.length, aulas.length)

    def minimoEnRango(ini: Int, fin: Int): (Asignacion, Int) = {
      if (fin - ini <= 1) {
        val a = candidatas(ini)
        (a, costoAsignacion(cursos, aulas, d, a, w))
      } else {
        val mid = ini + (fin - ini) / 2
        val (izq, der) = parallel(minimoEnRango(ini, mid), minimoEnRango(mid, fin))
        if (izq._2 <= der._2) izq else der
      }
    }

    minimoEnRango(0, candidatas.length)
  }
}