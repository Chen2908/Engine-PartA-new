package Model.Indexing;

import Model.Model;

import java.util.List;

/**
 * This Interface define an object that can be write to a file
 */
public interface IWritable {
    List<StringBuilder> toFile();
}
