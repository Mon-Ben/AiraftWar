import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;

class StrassenMatrixMultiplicationTest {

    private StrassenMatrixMultiplication solver;

    @BeforeEach
    void setUp() {
        solver = new StrassenMatrixMultiplication();
    }

    /* ========== 乘法核心 ========== */
    @Test
    @DisplayName("1×1 矩阵")
    void testMultiply1x1() {
        int[][] a = {{5}};
        int[][] b = {{7}};
        int[][] exp = {{35}};
        Assertions.assertArrayEquals(exp, solver.multiply(a, b));
    }

    @Test
    @DisplayName("2×2 矩阵")
    void testMultiply2x2() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{5, 6}, {7, 8}};
        int[][] exp = {{19, 22}, {43, 50}};
        Assertions.assertArrayEquals(exp, solver.multiply(a, b));
    }

    @Test
    @DisplayName("4×4 矩阵")
    void testMultiply4x4() {
        int[][] a = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 0, 1, 2},
                {3, 4, 5, 6}};
        int[][] b = {
                {6, 5, 4, 3},
                {2, 1, 0, 9},
                {8, 7, 6, 5},
                {4, 3, 2, 1}};
        int[][] exp = {
                {50, 40, 30, 40},
                {130, 104, 78, 112},
                {70, 58, 46, 34},
                {90, 72, 54, 76}};
        Assertions.assertArrayEquals(exp, solver.multiply(a, b));
    }

    /* ========== 加 / 减 ========== */
    @Test
    @DisplayName("矩阵加法")
    void testAdd() {
        int[][] a = {{1, 2}, {3, 4}};
        int[][] b = {{5, 6}, {7, 8}};
        int[][] exp = {{6, 8}, {10, 12}};
        Assertions.assertArrayEquals(exp, solver.add(a, b));
    }

    @Test
    @DisplayName("矩阵减法")
    void testSub() {
        int[][] a = {{5, 6}, {7, 8}};
        int[][] b = {{1, 2}, {3, 4}};
        int[][] exp = {{4, 4}, {4, 4}};
        Assertions.assertArrayEquals(exp, solver.sub(a, b));
    }

    /* ========== split / join ========== */
    @Test
    @DisplayName("split & join 互逆")
    void testSplitJoinInverse() {
        int[][] src = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 0, 1, 2},
                {3, 4, 5, 6}};
        int[][] dst = new int[4][4];
        int[][] c11 = new int[2][2], c12 = new int[2][2],
                c21 = new int[2][2], c22 = new int[2][2];

        solver.split(src, c11, 0, 0);
        solver.split(src, c12, 0, 2);
        solver.split(src, c21, 2, 0);
        solver.split(src, c22, 2, 2);

        solver.join(c11, dst, 0, 0);
        solver.join(c12, dst, 0, 2);
        solver.join(c21, dst, 2, 0);
        solver.join(c22, dst, 2, 2);

        Assertions.assertArrayEquals(src, dst);
    }
}