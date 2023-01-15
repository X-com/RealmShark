package experimental;

public class SpiralOut {

    public interface ISpiralOut {
        public boolean process(int x, int y);
    }

    ISpiralOut spiral;

    public SpiralOut(ISpiralOut p) {
        spiral = p;
    }

    public boolean spiral(int limitPoints, int limitRadius, int x, int y) {
        // (di, dj) is a vector - direction in which we move right now
        int di = 1;
        int dj = 0;
        // length of current segment
        int segmentLength = 1;
        int radius = 0;

        // current position (i, j) and how much of current segment we passed
        int i = 0;
        int j = 0;
        int segmentPassed = 0;
        for (int k = 0; k < limitPoints; ++k) {
            // make a step, add 'direction' vector (di, dj) to current position (i, j)
            if(spiral.process(i + x, j + y)) return true;
            i += di;
            j += dj;
            ++segmentPassed;

            if (segmentPassed == segmentLength) {
                // done with current segment
                radius++;
                segmentPassed = 0;
                if(limitRadius <= (radius / 4)) return false;

                // 'rotate' directions
                int buffer = di;
                di = -dj;
                dj = buffer;

                // increase segment length if necessary
                if (dj == 0) {
                    ++segmentLength;
                }
            }
        }

        return false;
    }
}
