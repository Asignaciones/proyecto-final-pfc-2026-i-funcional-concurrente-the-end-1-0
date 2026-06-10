package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import AsignacionAulas._
import AsignacionAulasPar._

@RunWith(classOf[JUnitRunner])
class AsignacionAulasParTest extends AnyFunSuite {

  val c1: Cursos     = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
  val a1: Aulas      = Vector(("E101", 30), ("E102", 40))
  val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
  val w: Pesos       = (1000, 100, 1, 2)

  // -------------------------------------------------------------------------
  // choquesPar
  // -------------------------------------------------------------------------

  // Verifica que la función detecte correctamente un choque.
  // M01 y M02 están en la misma aula y sus horarios se traslapan,
  // por lo que debe contarse exactamente un choque.
  test("choquesPar: asignacion [0,0,1] tiene 1 choque") {
    assert(choquesPar(c1, Vector(0, 0, 1)) == 1)
  }

  // Verifica que no se reporten choques cuando los cursos
  // que se traslapan están asignados a aulas diferentes.
  test("choquesPar: asignacion [0,1,0] no tiene choques") {
    assert(choquesPar(c1, Vector(0, 1, 0)) == 0)
  }

  // Compara la versión paralela con la secuencial.
  // Ambas deben producir exactamente el mismo número de choques.
  test("choquesPar coincide con choques secuencial en caso no trivial") {
    val cs = Vector(("A",0,10,10),("B",2,8,10),("C",4,6,10),("D",9,12,10))
    val asig = Vector(0, 0, 0, 1)
    assert(choquesPar(cs, asig) == choques(cs, asig))
  }

  // Caso límite: si no existen cursos,
  // entonces no puede existir ningún choque.
  test("choquesPar: sin cursos da 0") {
    assert(choquesPar(Vector.empty, Vector.empty) == 0)
  }

  // Escenario donde todos los cursos usan la misma aula.
  // Se verifica que el resultado coincida con la versión secuencial.
  test("choquesPar: todos los cursos en mismo salon") {
    val cs = Vector(("A",0,10,10),("B",2,8,10),("C",4,6,10))
    assert(choquesPar(cs, Vector(0,0,0)) == choques(cs, Vector(0,0,0)))
  }

  // -------------------------------------------------------------------------
  // desperdicioPar
  // -------------------------------------------------------------------------

  // Verifica que el desperdicio calculado coincida con el ejemplo
  // presentado en el enunciado del proyecto.
  test("desperdicioPar: asignacion [0,0,1] tiene desperdicio 25") {
    assert(desperdicioPar(c1, a1, Vector(0, 0, 1)) == 25)
  }

  // Compara la versión paralela con la secuencial para asegurar
  // que ambas calculen el mismo desperdicio.
  test("desperdicioPar coincide con desperdicio secuencial") {
    assert(desperdicioPar(c1, a1, Vector(0, 1, 0)) == desperdicio(c1, a1, Vector(0, 1, 0)))
  }

  // Caso límite: sin cursos y sin aulas,
  // el desperdicio total debe ser cero.
  test("desperdicioPar: vector vacio da 0") {
    assert(desperdicioPar(Vector.empty, Vector.empty, Vector.empty) == 0)
  }

  // Si la capacidad del aula coincide exactamente con la cantidad
  // de estudiantes, no debe existir desperdicio.
  test("desperdicioPar: capacidad exacta da 0") {
    val cs = Vector(("X", 0, 4, 30))
    val as = Vector(("E1", 30))
    assert(desperdicioPar(cs, as, Vector(0)) == 0)
  }

  // Caso más grande para validar que la versión paralela
  // siga produciendo el mismo resultado que la secuencial.
  test("desperdicioPar: coincide con secuencial para 6 cursos") {
    val cs = Vector.tabulate(6)(i => ("C"+i, i*2, i*2+2, 10+i))
    val as = Vector(("E1",40),("E2",35))
    val asig = Vector(0,1,0,1,0,1)
    assert(desperdicioPar(cs, as, asig) == desperdicio(cs, as, asig))
  }

  // -------------------------------------------------------------------------
  // movilidadPar
  // -------------------------------------------------------------------------

  // Verifica que la movilidad calculada coincida con el valor
  // esperado del ejemplo utilizado en el proyecto.
  test("movilidadPar: asignacion [0,0,1] tiene movilidad 3") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 0, 1)) == 3)
  }

  // Comprueba que la versión paralela y la secuencial produzcan
  // exactamente el mismo costo de movilidad.
  test("movilidadPar coincide con movilidad secuencial") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 1, 0)) == movilidad(c1, a1, d1, Vector(0, 1, 0)))
  }

  // Si solo existe un curso asignado, no hay desplazamientos
  // entre aulas y la movilidad debe ser cero.
  test("movilidadPar: un solo curso da 0") {
    assert(movilidadPar(c1, a1, d1, Vector(0, -1, -1)) == 0)
  }

  // Si todos los cursos usan la misma aula,
  // la distancia recorrida es cero.
  test("movilidadPar: misma aula da 0") {
    assert(movilidadPar(c1, a1, d1, Vector(0, 0, 0)) == 0)
  }

  // Verifica nuevamente la equivalencia entre la versión
  // paralela y la secuencial en un escenario más amplio.
  test("movilidadPar: coincide con secuencial para 4 cursos") {
    val cs = Vector(("A",0,2,10),("B",2,4,10),("C",4,6,10),("D",6,8,10))
    val asig = Vector(0,1,0,1)
    assert(movilidadPar(cs, a1, d1, asig) == movilidad(cs, a1, d1, asig))
  }

  // -------------------------------------------------------------------------
  // generarAsignacionesPar
  // -------------------------------------------------------------------------

  // Con 2 cursos y 2 aulas deben existir exactamente
  // 2**2 = 4 asignaciones posibles.
  test("generarAsignacionesPar: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignacionesPar(2, 2).length == 4)
  }

  // Verifica que la versión paralela genere exactamente
  // las mismas asignaciones que la versión secuencial.
  test("generarAsignacionesPar: mismo conjunto que version secuencial") {
    assert(generarAsignacionesPar(3, 2).toSet == generarAsignaciones(3, 2).toSet)
  }

  // Caso base de la recursión.
  // Sin cursos solo existe una asignación válida: el vector vacío.
  test("generarAsignacionesPar: 0 cursos produce 1 asignacion vacia") {
    assert(generarAsignacionesPar(0, 3) == Vector(Vector.empty))
  }

  // Comprueba que todas las asignaciones generadas tengan
  // una posición para cada curso del problema.
  test("generarAsignacionesPar: todas las asignaciones tienen longitud n") {
    assert(generarAsignacionesPar(4, 2).forall(_.length == 4))
  }

  // Con 3 cursos y 3 aulas deben generarse exactamente
  // 3**3 = 27 asignaciones posibles.
  test("generarAsignacionesPar: 3 cursos 3 aulas produce 27") {
    assert(generarAsignacionesPar(3, 3).length == 27)
  }

  // -------------------------------------------------------------------------
  // asignacionOptimaPar
  // -------------------------------------------------------------------------

  // Comprueba que la solución encontrada sea al menos tan buena
  // como una asignación conocida cuyo costo es 37.
  test("asignacionOptimaPar: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptimaPar(c1, a1, d1, w)
    assert(costo <= 37)
  }

  // Verifica que la búsqueda paralela encuentre el mismo costo óptimo
  // que la implementación secuencial.
  test("asignacionOptimaPar coincide con asignacionOptima en costo") {
    val (_, costoSec) = asignacionOptima(c1, a1, d1, w)
    val (_, costoPar) = asignacionOptimaPar(c1, a1, d1, w)
    assert(costoSec == costoPar)
  }

  // Comprueba que la asignación resultante tenga una posición
  // para cada curso del conjunto de entrada.
  test("asignacionOptimaPar: la asignacion tiene longitud igual a cursos") {
    val (asig, _) = asignacionOptimaPar(c1, a1, d1, w)
    assert(asig.length == c1.length)
  }

  // Debido a que los choques tienen una penalización muy alta,
  // la solución óptima debería evitar cualquier choque de horario.
  test("asignacionOptimaPar: prefiere asignacion sin choques") {
    val (asig, _) = asignacionOptimaPar(c1, a1, d1, w)
    assert(choques(c1, asig) == 0)
  }

  // Verifica una propiedad básica del modelo:
  // el costo total nunca puede ser negativo.
  test("asignacionOptimaPar: costo es no negativo") {
    val (_, costo) = asignacionOptimaPar(c1, a1, d1, w)
    assert(costo >= 0)
  }
}