package org.opengroup.osdu.schema.util;

import java.util.Comparator;

public class VersionHierarchyUtil {
    private Long schemaVersionMajor;
    private Long schemaVersionMinor;
    private Long schemaVersionPatch;

    public VersionHierarchyUtil(Long schemaVersionMajor, Long schemaVersionMinor, Long schemaVersionPatch) {
        this.schemaVersionMajor = schemaVersionMajor;
        this.schemaVersionMinor = schemaVersionMinor;
        this.schemaVersionPatch = schemaVersionPatch;
    }

    public static class SortingVersionComparator implements Comparator<VersionHierarchyUtil> {
        @Override
        public int compare(VersionHierarchyUtil v1, VersionHierarchyUtil v2) {
            int compareMajor = v1.schemaVersionMajor.compareTo(v2.schemaVersionMajor);
            if (compareMajor != 0)
                return compareMajor * (-1);
            else {
                int compareMinor = v1.schemaVersionMinor.compareTo(v2.schemaVersionMinor);
                if (compareMinor != 0)
                    return compareMinor * (-1);
                else
                    return v1.schemaVersionPatch.compareTo(v2.schemaVersionPatch) * (-1);
            }
        }
    }
}
