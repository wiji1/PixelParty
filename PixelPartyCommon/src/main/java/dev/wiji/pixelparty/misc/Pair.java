package dev.wiji.pixelparty.misc;

import java.util.Objects;

public class Pair<A, B> {
    private A first;
    private B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return this.first;
    }

    public B getSecond() {
        return this.second;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public void setSecond(B second) {
        this.second = second;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            Pair<?, ?> pair = (Pair)o;
            return Objects.equals(this.first, pair.first) && Objects.equals(this.second, pair.second);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.first, this.second});
    }
}