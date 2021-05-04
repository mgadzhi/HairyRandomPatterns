import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.Node
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.random.Random

enum class CellState {
    DEAD,
    ALIVE
}

typealias Coord = Pair<Int, Int>

fun Coord.isValid(width: Int, height: Int): Boolean {
    return listOf(
        first >= 0,
        first < height,
        second >= 0,
        second <= width
    ).all { it }

}

fun<T> cumulativeSums(xs: Iterable<T>): T {
    
}

class Field(val width: Int, val height: Int) {
    val cells: MutableList<CellState> = MutableList(width * height) { CellState.DEAD }
    init {
        cells[index(width / 2, height / 2)] = CellState.ALIVE
    }

    private fun index(row: Int, col: Int): Int {
        return row * width + col
    }

    private fun index(c: Coord): Int {
        return index(c.first, c.second)
    }

    private fun reverseIndex(i: Int): Coord {
        val row = i / width
        val col = i % width
        return Pair(row, col)
    }

    fun getAt(row: Int, col: Int): CellState {
        return cells[index(row, col)]
    }

    fun getAt(c: Coord): CellState {
        return cells[index(c)]
    }

    fun killAt(row: Int, col: Int) {
        cells[index(row, col)] = CellState.DEAD
    }

    fun killAt(c: Coord) {
        cells[index(c)] = CellState.DEAD
    }

    fun bornAt(row: Int, col: Int) {
        cells[index(row, col)] = CellState.ALIVE
    }

    fun bornAt(c: Coord) {
        cells[index(c)] = CellState.ALIVE
    }

    fun allAlive(): List<Coord> {
        val result = mutableListOf<Coord>()
        for (i in cells.indices) {
            if (cells[i] == CellState.ALIVE) {
                result.add(reverseIndex(i))
            }
        }
        return result
    }

    fun neighbors(c: Coord): List<Coord> {
        val up = Coord(c.first - 1, c.second)
        val down = Coord(c.first + 1, c.second)
        val left = Coord(c.first, c.second - 1)
        val right = Coord(c.first, c.second + 1)
        return listOf(up, down, left, right).filter { it.isValid(width, height) }
    }

    fun candidates(): Map<Coord, Int> {
        val result = mutableMapOf<Coord, Int>().withDefault { 0 }
        for (c in allAlive()) {
            for (n in neighbors(c).filter { getAt(it) == CellState.DEAD }) {
                result[n] = result.getValue(n) + 1
            }
        }
        println(result)
        return result
    }

    fun score(candidates: Map<Coord, Int>): Map<Coord, Double> {
        val n = candidates.size
        return candidates.mapValues { it.value.toDouble() / n }
    }

    fun scoredCandidates(): Map<Coord, Double> {
        return score(candidates())
    }
}

private fun drawCanvas() {
    val canvas = document.getElementById("hairy") as HTMLCanvasElement
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    val cellSize = 40.0
    val field = Field(10, 10)
    field.bornAt(7, 5)

    with(ctx) {
        beginPath()
//            fillStyle = listOf("red", "green", "orange", "blue").random()
//            rect(randomCoordinate(), randomCoordinate(), 20.0, 20.0)
        for (c in field.allAlive()) {
            fillStyle = "rgba(255, 0, 0, 1)"
            fillRect(c.first * cellSize, c.second * cellSize, cellSize, cellSize)
        }
        for (c in field.scoredCandidates()) {
            val coord = c.key
            val score = c.value
            fillStyle = "rgba(255, 0, 0, ${score})"
            fillRect(coord.first * cellSize, coord.second * cellSize, cellSize, cellSize)
        }
        closePath()
    }
}

private fun randomCoordinate() = Random.nextDouble(0.0, 200.0)

fun main() {
    window.onload = { drawCanvas() }
}
