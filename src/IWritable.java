import java.util.List;

public interface IWritable {
    List<String> toFile();
    List<String> update(List<String> toUpdate);
}
