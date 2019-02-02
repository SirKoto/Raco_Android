package com.koto.sir.racoenpfib.databases;

public class DBSchema {
    private DBSchema() {
    }

    public static final class AvisosTable {
        public static final String NAME = "avisos";

        private AvisosTable() {
        }

        public static final class Cols {
            public static final String UUID = "uuidAvis";
            public static final String TITLE = "titleAvis";
            public static final String ASSIGNATURA = "assignaturaAvis";
            public static final String TEXT = "textAvis";
            public static final String DATA_INSERCIO = "datainsertAvis";
            public static final String DATA_MODIFICACIO = "datamodificacioAvis";
            public static final String DATA_CADUCITAT = "datacaducitatAvis";
            public static final String IS_VIST = "isvistAvis";
            private Cols() {
            }
        }
    }

    public static final class AdjuntsTable {
        public static final String NAME = "adjunts";

        private AdjuntsTable() {
        }

        public static final class Cols {
            public static final String UUID_FOREIGN = "uuidadjunts";
            public static final String NAME = "nomadjunts";
            public static final String URL = "urladjunts";
            public static final String MIME = "mimetypeadjunts";
            public static final String DATA_MODIFICACIO = "datamodificatadjunts";
//            public static final String TAMANY = "tamanyadjunts";
            private Cols() {
            }
        }
    }

}
