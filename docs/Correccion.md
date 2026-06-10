# Informe de corrección — Proyecto Final

**Curso:** Fundamentos de Programación Funcional y Concurrente  
**Integrantes:** (completar con nombres, códigos y correos)

---

## Marco teórico

Para argumentar que un programa $P_f$ es correcto respecto a su especificación $f$, demostramos:

$$\forall x \in \text{Dom}(f) : P_f(x) = f(x)$$

Para funciones recursivas usamos **inducción estructural**. Para funciones de alto orden como `foldLeft` usamos el **invariante del acumulador**.

---

## 1. `solapan`

**Especificación:** $\text{solapan}(c_1, c_2) \iff \text{ini}_{c_1} < \text{fin}_{c_2} \land \text{ini}_{c_2} < \text{fin}_{c_1}$

**Corrección:** la implementación aplica directamente la condición. Dos intervalos $[a,b)$ y $[c,d)$ se solapan si y solo si $a < d \land c < b$. La implementación es correcta por definición. $\blacksquare$

---

## 2. `choques`

**Especificación:**

$$\text{choques}(cs, a) = |\{(i,j) \mid 0 \leq i < j < n,\; a_i = a_j \geq 0,\; \text{solapan}(cs_i, cs_j)\}|$$

**Corrección de `choquesConI(i, j)`:** por inducción sobre $n - j$.

- **Caso base** $j \geq n$: retorna 0. El conjunto de pares con segundo índice $\geq n$ es vacío. ✓
- **Caso inductivo**: supongamos que `choquesConI(i, j+1)` cuenta correctamente los pares $(i, k)$ con $k \geq j+1$. Entonces:

$$\text{choquesConI}(i, j) = [a_i = a_j \geq 0 \land \text{solapan}(i,j)] + \text{choquesConI}(i, j+1)$$

que cuenta exactamente los pares $(i, k)$ con $k \geq j$. ✓

**Corrección de `recorre(i)`:** por inducción sobre $n - 1 - i$.

- **Caso base** $i \geq n-1$: retorna 0. No quedan pares con primer índice $\geq n-1$. ✓
- **Caso inductivo**: supongamos que `recorre(i+1)` cuenta todos los pares $(k,l)$ con $k \geq i+1$. Entonces:

$$\text{recorre}(i) = \text{choquesConI}(i, i+1) + \text{recorre}(i+1)$$

que cuenta todos los pares $(i,j)$ con $j > i$, más todos los pares $(k,l)$ con $k > i$. Esto cubre todos los pares $(k,l)$ con $k \geq i$ y $l > k$. ✓ $\blacksquare$

---

## 3. `capacidadFallida`

**Especificación:**

$$\text{CF}(cs, as, a) = |\{i \mid a_i \geq 0 \land \text{cap}(as_{a_i}) < \text{est}(cs_i)\}|$$

**Corrección:** `foldLeft` con acumulador `acc` mantiene el invariante:

$$\text{Inv}(k, \text{acc}) \equiv \text{acc} = |\{i < k \mid a_i \geq 0 \land \text{cap}(as_{a_i}) < \text{est}(cs_i)\}|$$

- **Inicio:** $k=0$, $\text{acc}=0$. $\text{Inv}(0, 0)$: conjunto vacío $\Rightarrow 0$. ✓
- **Paso:** si $\text{Inv}(k, \text{acc})$ y se procesa $k$: si falla capacidad, $\text{acc}+1$; si no, $\text{acc}$. En ambos casos $\text{Inv}(k+1, \text{acc}')$. ✓
- **Final:** $\text{Inv}(n, \text{acc})$ implica $\text{acc} = \text{CF}(cs, as, a)$. ✓ $\blacksquare$

---

## 4. `desperdicio`

**Especificación:**

$$\text{DE}(cs, as, a) = \sum_{\substack{i=0 \\ a_i \geq 0}}^{n-1} \max(\text{cap}(as_{a_i}) - \text{est}(cs_i),\; 0)$$

**Corrección:** mismo argumento de invariante de `foldLeft`:

$$\text{Inv}(k, \text{acc}) \equiv \text{acc} = \sum_{\substack{i < k \\ a_i \geq 0}} \max(\text{cap}(as_{a_i}) - \text{est}(cs_i),\; 0)$$

El paso agrega `diff` si `diff >= 0`, 0 si no, preservando el invariante. $\blacksquare$

---

## 5. `movilidad`

**Especificación:** sea $\sigma$ el orden de los índices asignados por hora de inicio:

$$\text{MV}(cs, as, d, a) = \sum_{k=0}^{|\sigma|-2} d[a_{\sigma_k}][a_{\sigma_{k+1}}]$$

**Corrección de `sumaDistancias(idx, acc)`:** invariante de recursión de cola:

$$\text{Inv}(\text{idx}, \text{acc}) \equiv \text{acc} = \sum_{k=0}^{\text{idx}-1} d[a_{\sigma_k}][a_{\sigma_{k+1}}]$$

- **Inicio:** $\text{idx}=0$, $\text{acc}=0$. La suma vacía es 0. ✓
- **Paso:** $\text{Inv}(\text{idx}, \text{acc}) \Rightarrow \text{Inv}(\text{idx}+1, \text{acc} + d[\ldots][\ldots])$. ✓
- **Final:** cuando $\text{idx} \geq |\sigma|-1$, retorna $\text{acc} = \text{MV}$. ✓ $\blacksquare$

---

## 6. `costoAsignacion`

**Especificación:** $CT = w_1 \cdot CH + w_2 \cdot CF + w_3 \cdot DE + w_4 \cdot MV$

**Corrección:** directa por sustitución, usando la corrección de `choques`, `capacidadFallida`, `desperdicio` y `movilidad`. $\blacksquare$

---

## 7. `generarAsignaciones`

**Especificación:** $\text{gen}(n, m) = \{0,\ldots,m-1\}^n$ como vector de vectores.

**Corrección por inducción sobre $n$:**

- **Caso base** $n=0$: retorna $\{[]\}$, que es $\{0,\ldots,m-1\}^0 = \{[]\}$. ✓
- **Caso inductivo:** supongamos que $\text{gen}(n-1, m) = \{0,\ldots,m-1\}^{n-1}$. Entonces:

$$\text{gen}(n, m) = \bigcup_{j=0}^{m-1} \{j\} \times \{0,\ldots,m-1\}^{n-1} = \{0,\ldots,m-1\}^n$$

El `flatMap` sobre $j \in \{0,\ldots,m-1\}$ y el `map` que prepend $j$ construyen exactamente este conjunto. ✓

**Tamaño:** $|\text{gen}(n,m)| = m^n$, pues $|\text{gen}(0,m)| = 1$ y $|\text{gen}(n,m)| = m \cdot |\text{gen}(n-1,m)|$. $\blacksquare$

---

## 8. `asignacionOptima`

**Especificación:** $\text{opt}(cs, as, d, w) = \arg\min_{a \in \text{gen}(n,m)} CT(a)$

**Corrección de `buscarMinimo(idx, mejorAsig, mejorCosto)`:** invariante de recursión de cola:

$$\text{Inv}(\text{idx}, \text{mA}, \text{mC}) \equiv \text{mA} = \arg\min_{k < \text{idx}} CT(\text{cand}_k) \;\land\; \text{mC} = \min_{k < \text{idx}} CT(\text{cand}_k)$$

- **Inicio:** $\text{idx}=1$, $\text{mA}=\text{cand}_0$, $\text{mC}=CT(\text{cand}_0)$. ✓
- **Paso:** si $CT(\text{cand}_{\text{idx}}) < \text{mC}$, actualiza ambos; si no, preserva. En ambos casos $\text{Inv}(\text{idx}+1, \text{mA}', \text{mC}')$. ✓
- **Final:** $\text{Inv}(|\text{cand}|, \text{mA}, \text{mC})$ implica que $\text{mA}$ es el mínimo global. ✓ $\blacksquare$

---

## Casos de prueba adicionales

Los casos de prueba están en `AsignacionAulasTest.scala` y `AsignacionAulasParTest.scala`. Se incluyen al menos 5 casos por función, validando: solapamiento exacto en los bordes, asignaciones con y sin choques, capacidad justa vs. insuficiente, movilidad con un solo curso, y la optimalidad del mínimo encontrado.