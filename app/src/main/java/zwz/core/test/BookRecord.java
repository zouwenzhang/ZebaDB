package zwz.core.test;

import com.zeba.db.BaseRecord;
import com.zeba.db.annotation.DbColumn;
import com.zeba.db.annotation.DbColumnType;
import com.zeba.db.annotation.DbTable;

@DbTable("book")
public class BookRecord extends BaseRecord{
    @DbColumn(name = "bookName",type = DbColumnType.TEXT)
    private String name;
    @DbColumn(name = "type",type = DbColumnType.INTEGER)
    private Integer bookType;
    @DbColumn(name = "time",type = DbColumnType.TEXT)
    private String bookTime;

    @DbColumn(name = "bookData",type = DbColumnType.BLOB)
    private String bookData;

    @DbColumn(name = "bookDataqwe",type = DbColumnType.BLOB)
    private byte[] bookDataByte;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBookType() {
        return bookType;
    }

    public void setBookType(Integer bookType) {
        this.bookType = bookType;
    }

    public String getBookTime() {
        return bookTime;
    }

    public void setBookTime(String bookTime) {
        this.bookTime = bookTime;
    }

    public String getBookData() {
        return bookData;
    }

    public void setBookData(String bookData) {
        this.bookData = bookData;
    }

    public String getBookDataByteString() {
        try {
            return new String(bookDataByte,"utf-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getBookDataByte() {
        return bookDataByte;
    }

    public void setBookDataByte(byte[] bookDataByte) {
        this.bookDataByte = bookDataByte;
    }
}
