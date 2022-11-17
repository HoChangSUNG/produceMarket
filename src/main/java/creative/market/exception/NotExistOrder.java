package creative.market.exception;

public class NotExistOrder extends RuntimeException{
    public NotExistOrder() {
        super();
    }

    public NotExistOrder(String message) {
        super(message);
    }

    public NotExistOrder(String message, Throwable cause) {
        super(message, cause);
    }

    public NotExistOrder(Throwable cause) {
        super(cause);
    }

    protected NotExistOrder(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}