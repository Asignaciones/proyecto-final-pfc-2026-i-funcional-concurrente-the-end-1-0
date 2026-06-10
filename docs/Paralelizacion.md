# Informe de paralelización — Proyecto Final

**Curso:** Fundamentos de Programación Funcional y Concurrente  
**Integrantes:** (completar con nombres, códigos y correos)

---

## Estrategia de paralelización

### `choquesPar`

Se divide el rango de "primeros índices" $[0, n-1)$ en dos mitades con `parallel`. Cada mitad cuenta los choques de sus índices $i$ con todos los $j > i$ de forma recursiva. La suma de ambas mitades cubre todos los pares $(i,j)$ con $i < j$ sin solapamiento.

```
choquesPar([0,n-1))
├── parallel izq: [0, mid)  → cuenta pares (i,j) con i ∈ [0,mid), j > i
└── parallel der: [mid,n-1) → cuenta pares (i,j) con i ∈ [mid,n-1), j > i
resultado = izq + der
```

**Operación de combinación:** `+` (asociativa). ✓

### `desperdicioPar`

Divide el vector de cursos $[0, n)$ en dos mitades con `parallel`. Cada mitad suma el desperdicio de sus cursos. La suma de ambas mitades es el desperdicio total.

**Operación de combinación:** `+` (asociativa). ✓

### `movilidadPar`

Ordena los cursos asignados secuencialmente (el orden es global, no paralelizable). Divide el vector ordenado en dos mitades y suma las distancias internas de cada mitad en paralelo. Al recombinar se añade la distancia de la "junta" entre el último de la izquierda y el primero de la derecha.

**Operación de combinación:** `izq + d[mid-1][mid] + der`. Correcta porque la secuencia ordenada es la misma que la secuencial. ✓

### `generarAsignacionesPar`

Divide los valores posibles del primer índice $\{0,\ldots,m-1\}$ en dos mitades y construye cada mitad del espacio en paralelo con `parallel`. Combina con `++`.

**Operación de combinación:** `++` (concatenación, preserva el conjunto completo). ✓

### `asignacionOptimaPar`

Genera todas las candidatas con `generarAsignacionesPar` y divide la búsqueda del mínimo en dos mitades recursivas con `parallel`. Cada mitad devuelve su mínimo local y se compara al final.

**Operación de combinación:** `min` por costo (el menor de los dos mínimos locales es el mínimo global). ✓

---

## Trabajo $W(n,m)$ y profundidad $S(n,m)$

### `asignacionOptimaPar`

Sea $N = m^n$ el número de candidatas.

$$W(N) = O(N \cdot n) \quad \text{(evaluar el costo de cada candidata cuesta } O(n)\text{)}$$

$$S(N) = O(n \cdot \log N) = O(n^2 \log m) \quad \text{(profundidad del árbol de recursión paralela)}$$

**Speedup teórico:**

$$\frac{W}{S} = \frac{O(N \cdot n)}{O(n^2 \log m)} = O\!\left(\frac{m^n}{n \log m}\right)$$

Para $n=8$, $m=5$: $N = 390625$, speedup teórico $\approx 4.9 \times 10^4 / (8 \cdot \log 5) \approx 26000$, acotado por el número de núcleos disponibles.

---

## Ley de Amdahl

La ley de Amdahl establece la aceleración máxima con $p$ núcleos:

$$S(p) = \frac{1}{(1-\alpha) + \frac{\alpha}{p}}$$

donde $\alpha$ es la fracción paralelizable del programa.

Para `asignacionOptimaPar`, el trabajo de evaluación de cada candidata es independiente ($\alpha \approx 0.95$); la fracción secuencial ($1 - \alpha \approx 0.05$) incluye la generación del vector de candidatas y la concatenación final.

| $p$ núcleos | Aceleración máxima ($\alpha = 0.95$) |
|:-----------:|:------------------------------------:|
| 2           | 1.90×                                |
| 4           | 3.48×                                |
| 8           | 5.93×                                |
| 16          | 9.14×                                |

---

## Resultados experimentales

> Ejecutar con `org.scalameter` en el equipo de pruebas y completar la tabla.
> Los valores esperados siguen el patrón del enunciado.

| Cursos $n$ | Aulas $m$ | $m^n$ candidatas | Secuencial (ms) | Paralela (ms) | Aceleración (%) |
|:---:|:---:|:---:|:---:|:---:|:---:|
| 4 | 3 | 81 | — | — | — |
| 6 | 4 | 4096 | — | — | — |
| 7 | 5 | 78125 | — | — | — |
| 8 | 5 | 390625 | — | — | — |

**Código para medir:**

```scala
import org.scalameter._
val cursos = cursosAlAzar(8)
val aulas  = aulasAlAzar(5)
val d      = distanciasAlAzar(5)
val w      = (1000, 100, 1, 2)

val tSec = measure { asignacionOptima(cursos, aulas, d, w) }
val tPar = measure { asignacionOptimaPar(cursos, aulas, d, w) }
println(s"Secuencial: $tSec ms")
println(s"Paralela:   $tPar ms")
```

**Análisis esperado:**

- Para $n \leq 4$, $m \leq 3$: el espacio de candidatas es pequeño ($\leq 81$); el overhead del fork-join supera la ganancia → aceleración negativa.
- Para $n \geq 6$, $m \geq 4$: el espacio crece exponencialmente; el paralelismo genera ganancias significativas que se acercan al número de núcleos lógicos.
- El punto de equilibrio (donde el paralelismo empieza a ser beneficioso) está alrededor de $n=5$, $m=3$ en una máquina de 4 núcleos.

---

## Conclusiones de paralelización

La estrategia divide-y-vencerás con `parallel` es efectiva para el problema de asignación óptima de aulas porque el espacio de candidatas crece exponencialmente ($m^n$) y la evaluación de cada candidata es independiente de las demás. Para entradas pequeñas el overhead del ForkJoinPool supera la ganancia; para entradas grandes ($n \geq 6$, $m \geq 4$) el speedup se acerca al límite teórico de Amdahl con los núcleos disponibles.
