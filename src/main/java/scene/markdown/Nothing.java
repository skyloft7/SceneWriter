package scene.markdown;

public final class Nothing {
    private Nothing(){}

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Nothing);
    }
}
