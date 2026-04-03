package mapping;

import java.util.List;

public class MappingResult {

    public List<String[]> mappedData;
    public List<String> unmappedColumns;

    public MappingResult(List<String[]> mappedData, List<String> unmappedColumns) {
        this.mappedData = mappedData;
        this.unmappedColumns = unmappedColumns;
    }
}