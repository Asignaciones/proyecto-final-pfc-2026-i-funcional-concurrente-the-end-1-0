package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import AsignacionAulas._

@RunWith(classOf[JUnitRunner])
class AsignacionAulasTest extends AnyFunSuite {

  //Datos del ejemplo 1 del enunciado
  val c1: Cursos     = Vector(("M01", 4, 8, 25), ("M02", 6, 10, 30), ("M03", 12, 16, 20))
  val a1: Aulas      = Vector(("E101", 30), ("E102", 40))
  val d1: Distancias = Vector(Vector(0, 3), Vector(3, 0))
  val w: Pesos       = (1000, 100, 1, 2)

  // solapan

  // Verifica que la función detecte correctamente cuando dos cursos
  // tienen horarios que se traslapan.
  test("solapan: M01[4,8) y M02[6,10) se solapan") {
    assert(solapan(("M01", 4, 8, 25), ("M02", 6, 10, 30)))
  }

  // Verifica que la función retorne falso cuando los horarios
  // están completamente separados.
  test("solapan: M01[4,8) y M03[12,16) no se solapan") {
    assert(!solapan(("M01", 4, 8, 25), ("M03", 12, 16, 20)))
  }

  // Comprueba que dos cursos consecutivos no se consideran solapados
  // cuando uno termina exactamente donde empieza el otro.
  test("solapan: cursos adyacentes [0,4) y [4,8) no se solapan") {
    assert(!solapan(("A", 0, 4, 10), ("B", 4, 8, 10)))
  }

  // Verifica que exista solapamiento cuando un curso está contenido
  // completamente dentro del intervalo de otro.
  test("solapan: un curso contenido en otro se solapa") {
    assert(solapan(("A", 2, 8, 10), ("B", 3, 6, 10)))
  }

  // Caso límite donde el inicio de un curso coincide exactamente
  // con el final del otro. No debe considerarse solapamiento.
  test("solapan: inicio igual al fin del otro no es solapamiento") {
    assert(!solapan(("A", 0, 5, 10), ("B", 5, 10, 10)))
  }

  // choques

  // Verifica que se detecte un choque cuando dos cursos comparten
  // aula y además sus horarios se traslapan.
  test("choques: asignacion [0,0,1] tiene 1 choque (M01 y M02 en E101)") {
    assert(choques(c1, Vector(0, 0, 1)) == 1)
  }

  // Comprueba que no existan choques cuando los cursos solapados
  // están asignados a aulas diferentes.
  test("choques: asignacion [0,1,0] no tiene choques") {
    assert(choques(c1, Vector(0, 1, 0)) == 0)
  }

  // Escenario extremo donde todos los cursos están en la misma aula
  // y todos se traslapan entre sí.
  test("choques: todos en el mismo salon con solapamiento total") {
    val cs = Vector(("A", 0, 10, 10), ("B", 2, 8, 10), ("C", 4, 6, 10))
    assert(choques(cs, Vector(0, 0, 0)) == 3) // pares (0,1),(0,2),(1,2)
  }

  // Caso límite: sin cursos no puede existir ningún choque.
  test("choques: sin cursos, sin choques") {
    assert(choques(Vector.empty, Vector.empty) == 0)
  }

  // Con un solo curso no existe otro curso con quien chocar.
  test("choques: un solo curso, sin choques") {
    assert(choques(Vector(("A", 0, 4, 10)), Vector(0)) == 0)
  }


  // capacidadFallida

  // Verifica que ningún curso exceda la capacidad de su aula.
  test("capacidadFallida: asignacion [0,0,1] no falla capacidad") {
    assert(capacidadFallida(c1, a1, Vector(0, 0, 1)) == 0)
  }

  // Comprueba que se detecte cuando un curso tiene más estudiantes
  // que la capacidad disponible del aula.
  test("capacidadFallida: curso con mas estudiantes que capacidad del aula") {
    val cs = Vector(("X", 0, 4, 50))
    val as = Vector(("E1", 30))
    assert(capacidadFallida(cs, as, Vector(0)) == 1)
  }

  // La capacidad exacta del aula debe considerarse válida.
  test("capacidadFallida: capacidad exacta no falla") {
    val cs = Vector(("X", 0, 4, 30))
    val as = Vector(("E1", 30))
    assert(capacidadFallida(cs, as, Vector(0)) == 0)
  }

  // Verifica que la función cuente correctamente múltiples cursos
  // que incumplen la capacidad.
  test("capacidadFallida: dos cursos fallan") {
    val cs = Vector(("A", 0, 4, 50), ("B", 4, 8, 40))
    val as = Vector(("E1", 30))
    assert(capacidadFallida(cs, as, Vector(0, 0)) == 2)
  }

  // Caso límite: sin cursos no existen fallos de capacidad.
  test("capacidadFallida: vector vacio da 0") {
    assert(capacidadFallida(Vector.empty, Vector.empty, Vector.empty) == 0)
  }


  // desperdicio

  // Verifica el desperdicio calculado en el ejemplo principal
  // utilizado en el proyecto.
  test("desperdicio: asignacion [0,0,1] tiene desperdicio 25") {
    assert(desperdicio(c1, a1, Vector(0, 0, 1)) == 25)
  }

  // Comprueba que una asignación diferente produzca el mismo
  // desperdicio total esperado.
  test("desperdicio: asignacion [0,1,0] tiene desperdicio 25") {
    assert(desperdicio(c1, a1, Vector(0, 1, 0)) == 25)
  }

  // Si la capacidad coincide exactamente con la cantidad de estudiantes,
  // no debe existir desperdicio.
  test("desperdicio: capacidad exacta da desperdicio 0") {
    val cs = Vector(("X", 0, 4, 30))
    val as = Vector(("E1", 30))
    assert(desperdicio(cs, as, Vector(0)) == 0)
  }

  // Cuando la capacidad es insuficiente no se contabiliza desperdicio,
  // ya que no existen puestos vacíos.
  test("desperdicio: curso con insuficiencia no suma al desperdicio") {
    val cs = Vector(("X", 0, 4, 50))
    val as = Vector(("E1", 30))
    assert(desperdicio(cs, as, Vector(0)) == 0)
  }

  // Verifica que el desperdicio se acumule correctamente cuando
  // existen varios cursos asignados.
  test("desperdicio: suma correcta con multiples cursos") {
    val cs = Vector(("A", 0, 4, 10), ("B", 4, 8, 20))
    val as = Vector(("E1", 40))
    // 40-10=30, 40-20=20 → 50
    assert(desperdicio(cs, as, Vector(0, 0)) == 50)
  }

  // movilidad

  // Con un solo curso no existe desplazamiento entre aulas.
  test("movilidad: un solo curso tiene movilidad 0") {
    assert(movilidad(c1, a1, d1, Vector(0, -1, -1)) == 0)
  }

  // Verifica el cálculo de movilidad usando el ejemplo
  // principal del proyecto.
  test("movilidad: asignacion [0,0,1] tiene movilidad 3") {
    // orden: M01(ini=4,aula=0) → M02(ini=6,aula=0) → M03(ini=12,aula=1)
    // d[0][0]+d[0][1] = 0+3 = 3
    assert(movilidad(c1, a1, d1, Vector(0, 0, 1)) == 3)
  }

  // Si todos los cursos utilizan la misma aula,
  // la movilidad total debe ser cero.
  test("movilidad: misma aula consecutiva tiene distancia 0") {
    assert(movilidad(c1, a1, d1, Vector(0, 0, 0)) == 0)
  }

  // Caso límite: sin cursos no existe movilidad.
  test("movilidad: vector vacio da 0") {
    assert(movilidad(Vector.empty, Vector.empty, Vector.empty, Vector.empty) == 0)
  }

  // Comprueba que se calcule correctamente la distancia
  // entre dos aulas diferentes.
  test("movilidad: dos cursos en aulas distintas") {
    val cs = Vector(("A", 0, 4, 10), ("B", 4, 8, 10))
    assert(movilidad(cs, a1, d1, Vector(0, 1)) == 3)
  }

  // -------------------------------------------------------------------------
  // costoAsignacion
  // -------------------------------------------------------------------------

  // Verifica que el costo total coincida con el valor calculado
  // manualmente para el ejemplo del proyecto.
  test("costoAsignacion: asignacion [0,0,1] cuesta 1031") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 0, 1), w) == 1031)
  }

  // Comprueba el costo de una asignación sin choques
  // y con menor penalización.
  test("costoAsignacion: asignacion [0,1,0] cuesta 37") {
    assert(costoAsignacion(c1, a1, d1, Vector(0, 1, 0), w) == 37)
  }

  // Si no existen penalizaciones de ningún tipo,
  // el costo total debe ser cero.
  test("costoAsignacion: sin choques ni desperdicio ni movilidad da 0") {
    val cs = Vector(("A", 0, 4, 30))
    val as = Vector(("E1", 30))
    val dd = Vector(Vector(0))
    assert(costoAsignacion(cs, as, dd, Vector(0), w) == 0)
  }

  // generarAsignaciones

  // Con 2 cursos y 2 aulas deben existir exactamente
  // 2**2 = 4 asignaciones posibles.
  test("generarAsignaciones: 2 cursos y 2 aulas produce 4 asignaciones") {
    assert(generarAsignaciones(2, 2).length == 4)
  }

  // Con 3 cursos y 3 aulas deben generarse exactamente
  // 3**3 = 27 combinaciones.
  test("generarAsignaciones: 3 cursos y 3 aulas produce 27 asignaciones") {
    assert(generarAsignaciones(3, 3).length == 27)
  }

  // Caso base de la recursión.
  // Sin cursos solo existe una asignación vacía.
  test("generarAsignaciones: 0 cursos produce 1 asignacion vacia") {
    assert(generarAsignaciones(0, 3) == Vector(Vector.empty))
  }

  // Verifica que todas las asignaciones tengan una posición
  // para cada curso.
  test("generarAsignaciones: todas las asignaciones tienen longitud n") {
    assert(generarAsignaciones(4, 2).forall(_.length == 4))
  }

  // Comprueba que los valores generados correspondan
  // únicamente a índices válidos de aulas.
  test("generarAsignaciones: todos los valores estan en [0, m-1]") {
    assert(generarAsignaciones(3, 3).forall(_.forall(v => v >= 0 && v <= 2)))
  }

  
  // asignacionOptima

  // Verifica que la solución encontrada sea al menos tan buena
  // como una asignación conocida con costo 37.
  test("asignacionOptima: el costo de la optima no supera el de [0,1,0] (37)") {
    val (_, costo) = asignacionOptima(c1, a1, d1, w)
    assert(costo <= 37)
  }

  // El costo óptimo nunca debe ser negativo.
  test("asignacionOptima: el costo optimo es positivo o cero") {
    val (_, costo) = asignacionOptima(c1, a1, d1, w)
    assert(costo >= 0)
  }

  // Comprueba que la solución devuelta contenga una asignación
  // para cada curso existente.
  test("asignacionOptima: la asignacion tiene longitud igual a cursos") {
    val (asig, _) = asignacionOptima(c1, a1, d1, w)
    assert(asig.length == c1.length)
  }

  // Caso sencillo con un único curso para verificar
  // que la búsqueda encuentre el costo mínimo esperado.
  test("asignacionOptima: con un solo curso la optima tiene costo minimo") {
    val cs = Vector(("A", 0, 4, 25))
    val as = Vector(("E1", 30))
    val dd = Vector(Vector(0))
    val (_, costo) = asignacionOptima(cs, as, dd, w)
    assert(costo == 5) // desperdicio = 30-25 = 5
  }

  // Debido al alto peso de los choques, la solución óptima
  // debería evitar cualquier conflicto de horario.
  test("asignacionOptima: prefiere asignacion sin choques") {
    val (asig, _) = asignacionOptima(c1, a1, d1, w)
    assert(choques(c1, asig) == 0)
  }
}
