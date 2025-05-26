package khoroshkin.coursework;

public class Analysis {
    private String error = null;
    private AnalysisResult attributes;
    private AnalysisData data;
    private String status;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public AnalysisResult getAttributes() {
        return attributes;
    }

    public void setAttributes(AnalysisResult attributes) {
        this.attributes = attributes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AnalysisData getData() {
        return data;
    }

    public void setData(AnalysisData data) {
        this.data = data;
    }

    class AnalysisResult {
        dataStats stats;

        public dataStats getStats() {
            return stats;
        }

        public void setStats(dataStats stats) {
            this.stats = stats;
        }

        class dataStats {
            private String undetected;
            private String malicious;
            private String suspicious;
            private String unsupported;

            public String getUndetected() {
                return undetected;
            }

            public void setUndetected(String undetected) {
                this.undetected = undetected;
            }

            public String getMalicious() {
                return malicious;
            }

            public void setMalicious(String malicious) {
                this.malicious = malicious;
            }

            public String getSuspicious() {
                return suspicious;
            }

            public void setSuspicious(String suspicious) {
                this.suspicious = suspicious;
            }

            public String getUnsupported() {
                return unsupported;
            }

            public void setUnsupported(String unsupported) {
                this.unsupported = unsupported;
            }
        }
    }
    class AnalysisData {
        private String type;
        private String id;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
