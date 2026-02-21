package com.example.digitalapproval.dto;

import jakarta.validation.constraints.Size;

public class RequestDecisionDto {

        @Size(max = 500, message = "Remarks must be at most 500 characters")
        private String remarks;

        public RequestDecisionDto() {
        }

        public String getRemarks() {
                return remarks;
        }

        public String remarks() {
                return remarks;
        }

        public void setRemarks(String remarks) {
                this.remarks = remarks;
        }
}
