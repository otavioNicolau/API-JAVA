package com.pdfprocessor.infrastructure.adapter;

import com.pdfprocessor.domain.model.Job;
import com.pdfprocessor.domain.model.JobOperation;
import java.util.List;
import java.util.Map;

public class TestErrorMessages {
    public static void main(String[] args) {
        PdfProcessingServiceImpl service = new PdfProcessingServiceImpl();
        
        // Test 1: Missing text for add_text
        Job job1 = new Job("test-job", JobOperation.PDF_EDIT, List.of("input.pdf"), Map.of("edit_type", "add_text", "page", 1));
        
        try {
            service.processJob(job1);
        } catch (Exception e) {
            System.out.println("Test 1 - Missing text error: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Test 1 - Cause: " + e.getCause().getMessage());
            }
        }
        
        // Test 2: Unsupported edit type
        Job job2 = new Job("test-job", JobOperation.PDF_EDIT, List.of("input.pdf"), Map.of("edit_type", "invalid_type"));
        
        try {
            service.processJob(job2);
        } catch (Exception e) {
            System.out.println("Test 2 - Unsupported edit type error: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Test 2 - Cause: " + e.getCause().getMessage());
            }
        }
        
        // Test 3: Missing new text for replace_text
        Job job3 = new Job("test-job", JobOperation.PDF_EDIT, List.of("input.pdf"), Map.of("edit_type", "replace_text", "old_text", "old"));
        
        try {
            service.processJob(job3);
        } catch (Exception e) {
            System.out.println("Test 3 - Missing new text error: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Test 3 - Cause: " + e.getCause().getMessage());
            }
        }
    }
}