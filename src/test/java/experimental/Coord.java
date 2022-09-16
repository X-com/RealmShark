package experimental;

public class Coord {
    public int x;
    public int y;

    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coord add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Coord add(int a) {
        this.x += a;
        this.y += a;
        return this;
    }

    public Coord add(Coord c) {
        this.x += c.x;
        this.y += c.y;
        return this;
    }

    public Coord sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Coord sub(int a) {
        this.x -= a;
        this.y -= a;
        return this;
    }

    public Coord sub(Coord c) {
        this.x -= c.x;
        this.y -= c.y;
        return this;
    }

    public Coord mult(int x, int y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Coord mult(int a) {
        this.x *= a;
        this.y *= a;
        return this;
    }

    public Coord mult(Coord m) {
        this.x *= m.x;
        this.y *= m.y;
        return this;
    }

    public Coord div(int x, int y) {
        this.x /= x;
        this.y /= y;
        return this;
    }

    public Coord div(int a) {
        this.x /= a;
        this.y /= a;
        return this;
    }

    public Coord div(Coord c) {
        this.x /= c.x;
        this.y /= c.y;
        return this;
    }

    public Coord swap() {
        int temp = x;
        x = y;
        y = temp;
        return this;
    }

    public Coord abs() {
        x = Math.abs(x);
        y = Math.abs(y);
        return this;
    }

    public int sum() {
        return x + y;
    }

    public int dot() {
        return x * y;
    }

    public boolean equals(Coord c) {
        return this.x == c.x && this.y == c.y;
    }
}
