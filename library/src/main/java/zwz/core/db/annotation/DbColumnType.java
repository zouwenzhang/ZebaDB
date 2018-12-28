package zwz.core.db.annotation;

public enum DbColumnType implements DbColumnTypeInterface {
    INTEGER("INTEGER"), TEXT("TEXT");

    // 成员变量
    private String type;
    // 构造方法
    private DbColumnType(String type) {
        this.type = type;
    }
    @Override
    public String getType() {
        return this.type;
    }
}
