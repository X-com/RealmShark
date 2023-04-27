package assets;

public class AssetMissingException extends Exception {
    public AssetMissingException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
