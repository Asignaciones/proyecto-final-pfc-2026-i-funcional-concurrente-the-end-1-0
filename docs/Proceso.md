# Informe de proceso — Proyecto Final

**Curso:** Fundamentos de Programación Funcional y Concurrente  
**Integrantes:** (completar con nombres, códigos y correos)

---

## 1. `solapan`

Función no recursiva. Aplica directamente la condición matemática de solapamiento de intervalos:

$$\text{solapan}(c_1, c_2) \iff \text{ini}_{c_1} < \text{fin}_{c_2} \;\land\; \text{ini}_{c_2} < \text{fin}_{c_1}$$

---

## 2. `choques` — recursión lineal anidada

```scala
def choques(cursos: Cursos, a: Asignacion): Int = {
  def choquesConI(i: Int, j: Int): Int = ...
  def recorre(i: Int): Int = ...

  recorre(0)
}
```

**Ejemplo:** `cursos = [M01(4,8,25), M02(6,10,30), M03(12,16,20)]`, `a = [0,0,1]`

**Pila de llamadas de `recorre`:**

```mermaid
sequenceDiagram
    participant R as recorre(0)
    participant CI0 as choquesConI(0,1)
    participant CI1 as choquesConI(0,2)
    participant R2 as recorre(1)
    participant CI2 as choquesConI(1,2)
    participant R3 as recorre(2)

    R->>CI0: i=0, j=1 → a(0)==a(1)==0, solapan(M01,M02)=true → 1
    CI0->>CI1: i=0, j=2 → a(0)=0 ≠ a(2)=1 → 0
    CI1-->>R: retorna 1
    R->>R2: recorre(1)
    R2->>CI2: i=1, j=2 → a(1)=0 ≠ a(2)=1 → 0
    CI2-->>R2: retorna 0
    R2->>R3: recorre(2) → 2 >= n-1=2 → 0
    R3-->>R2: 0
    R2-->>R: 0
    R-->>R: resultado = 1+0 = 1
```

**Resultado:** `choques = 1` ✓

---

## 3. `capacidadFallida` — `foldLeft` sobre índices

Función de alto orden con `foldLeft`. Para cada índice `i`, si `a(i) >= 0` y `capAula < estCurso`, incrementa el acumulador.

**Traza para `a = [0,0,1]`, `aulas = [(E101,30),(E102,40)]`:**

| i | a(i) | cap | est | ¿falla? | acc |
|---|------|-----|-----|---------|-----|
| 0 | 0    | 30  | 25  | No      | 0   |
| 1 | 0    | 30  | 30  | No      | 0   |
| 2 | 1    | 40  | 20  | No      | 0   |

**Resultado:** `capacidadFallida = 0` ✓

---

## 4. `desperdicio` — `foldLeft` sobre índices

Similar a `capacidadFallida`. Suma `cap - est` cuando `cap >= est`.

**Traza para `a = [0,0,1]`:**

| i | cap | est | diff | acc |
|---|-----|-----|------|-----|
| 0 | 30  | 25  | 5    | 5   |
| 1 | 30  | 30  | 0    | 5   |
| 2 | 40  | 20  | 20   | 25  |

**Resultado:** `desperdicio = 25` ✓

---

## 5. `movilidad` — recursión de cola `sumaDistancias`

Primero ordena los cursos asignados por hora de inicio. Luego usa recursión de cola para sumar distancias consecutivas.

**Ejemplo:** `a = [0,0,1]`, orden por ini: M01(ini=4) → M02(ini=6) → M03(ini=12)

```mermaid
sequenceDiagram
    participant S0 as sumaDistancias(0, 0)
    participant S1 as sumaDistancias(1, d[0][0]+0=0+0=0... )
    participant S2 as sumaDistancias(2, 3)

    S0->>S1: idx=0, aulaActual=a[0]=0, aulaSig=a[1]=0, d[0][0]=0 → acc=0
    S1->>S2: idx=1, aulaActual=a[1]=0, aulaSig=a[2]=1, d[0][1]=3 → acc=3
    S2-->>S0: idx=2 >= length-1=2 → retorna 3
```

**Resultado:** `movilidad = 3` ✓

---

## 6. `costoAsignacion`

Combina las cuatro funciones anteriores con los pesos. No es recursiva.

**Para `a=[0,0,1]`, `w=(1000,100,1,2)`:**

$$CT = 1000 \cdot 1 + 100 \cdot 0 + 1 \cdot 25 + 2 \cdot 3 = 1031$$

---

## 7. `generarAsignaciones` — recursión lineal

**Especificación:** genera todos los vectores en $\{0,\ldots,m-1\}^n$.

**Traza para `n=2`, `m=2`:**

```mermaid
sequenceDiagram
    participant G2 as generarAsig(2,2)
    participant G1 as generarAsig(1,2)
    participant G0 as generarAsig(0,2)

    G2->>G1: llamada recursiva
    G1->>G0: llamada recursiva
    G0-->>G1: Vector(Vector())
    G1-->>G2: Vector(Vector(0), Vector(1))
    G2-->>G2: flatMap j∈{0,1}: prepend j → Vector([0,0],[0,1],[1,0],[1,1])
```

**Resultado:** 4 asignaciones = $2^2$ ✓

---

## 8. `asignacionOptima` — recursión de cola `buscarMinimo`

Genera todas las candidatas y las recorre con recursión de cola manteniendo la mejor asignación vista.

**Traza simplificada para el Ejemplo 1 del enunciado:**

```mermaid
sequenceDiagram
    participant B0 as buscarMinimo(1, [0,0,0], costo0)
    participant B1 as buscarMinimo(2, mejor1, costo1)
    participant BN as buscarMinimo(N, óptima, costoMin)

    B0->>B1: evalúa candidata(1), actualiza si mejora
    B1->>BN: continúa hasta agotar candidatas
    BN-->>B0: retorna (óptima, costoMin)
```

La asignación `[0,1,0]` con costo 37 resulta ser la óptima para el Ejemplo 1.s una representación más eficiente del espacio de candidatas (generación lazy con `Stream` o `Iterator`) para evitar materializar $m^n$ vectores en memoria antes de evaluar el costo de cada uno. Esto permitiría escalar a $n > 10$ sin agotar la memoria del heap.