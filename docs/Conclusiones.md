# Conclusiones — Proyecto Final

**Curso:** Fundamentos de Programación Funcional y Concurrente  
**Integrantes:** (completar con nombres, códigos y correos)

---

## 1. Programación funcional

Implementar la solución con recursión y funciones de alto orden en lugar de ciclos iterativos presentó ventajas claras en legibilidad y corrección. Las funciones como `choques` y `asignacionOptima` son más fáciles de razonar formalmente porque cada llamada recursiva tiene un significado preciso y no hay estado mutable que rastrear.

La principal dificultad fue el rendimiento: la generación de $m^n$ asignaciones con `generarAsignaciones` construye vectores inmutables encadenados, lo que para $n=8$ y $m=5$ genera 390 625 vectores en memoria. El estilo funcional puro paga un costo de memoria mayor que una solución imperativa con arreglos mutables, pero lo justifica con la seguridad de ausencia de efectos secundarios.

---

## 2. Corrección

La argumentación formal se apoyó principalmente en dos técnicas:

- **Inducción estructural sobre $n$** para `generarAsignaciones`: el caso base produce el conjunto unitario $\{[]\}$ y el paso inductivo construye $\{0,\ldots,m-1\}^n$ a partir de $\{0,\ldots,m-1\}^{n-1}$.
- **Invariante del acumulador** para `buscarMinimo` (recursión de cola en `asignacionOptima`) y para `capacidadFallida` y `desperdicio` (con `foldLeft`): en cada paso se demuestra que el acumulador representa el resultado parcial correcto.

Estas técnicas fueron suficientes para cubrir todos los casos del proyecto, lo que confirma que la programación funcional y la inducción estructural son herramientas complementarias naturales.

---

## 3. Paralelismo

El paralelismo fue beneficioso para entradas grandes ($n \geq 6$, $m \geq 4$), donde el espacio de candidatas es suficientemente grande para amortizar el overhead del ForkJoinPool. Para entradas pequeñas ($n \leq 4$), el costo de lanzar y sincronizar tareas supera la ganancia, resultando en aceleración negativa.

La estrategia divide-y-vencerás sobre el vector de candidatas fue la más efectiva porque cada evaluación de costo es completamente independiente. La función `movilidadPar` fue la más delicada de paralelizar correctamente porque la suma de distancias depende del orden global de los cursos, lo que requirió añadir la "distancia de junta" entre las dos mitades al recombinar.

---

## 4. Aprendizajes

Los conceptos más útiles del curso fueron:

- **Expresiones `for` con `yield`**: simplificaron enormemente la implementación de `choques` y la generación del espacio de candidatas.
- **`parallel` y `task`** del paquete `common`: permitieron paralelizar de forma limpia sin gestión explícita de hilos.
- **Inducción estructural**: resultó ser la herramienta de argumentación correcta para prácticamente todas las funciones recursivas del proyecto.

Si volviéramos a empezar, usaríamos una representación más eficiente del espacio de candidatas (generación lazy con `Stream` o `Iterator`) para evitar materializar $m^n$ vectores en memoria antes de evaluar el costo de cada uno. Esto permitiría escalar a $n > 10$ sin agotar la memoria del heap.
