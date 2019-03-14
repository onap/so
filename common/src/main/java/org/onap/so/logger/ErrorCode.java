package org.onap.so.logger;

public enum ErrorCode {
    PermissionError(100), AvailabilityError(200), DataError(300), SchemaError(400), BusinessProcesssError(
        500), UnknownError(900);

    private int value;

    ErrorCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
