  package com.events.event_management.service;

import com.events.event_management.model.Attendance;
import com.events.event_management.model.Club;
import com.events.event_management.model.Event;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
    
    /**
     * Export events to Excel
     */
    public byte[] exportEventsToExcel(List<Event> events) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Events");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Title", "Description", "Date", "Venue", "Club", 
                               "Organizer", "Participants", "Status"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Event event : events) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(event.getId() != null ? event.getId() : 0);
                row.createCell(1).setCellValue(event.getTitle() != null ? event.getTitle() : "");
                row.createCell(2).setCellValue(event.getDescription() != null ? event.getDescription() : "");
                row.createCell(3).setCellValue(event.getEventDate() != null ? event.getEventDate().format(DATE_FORMATTER) : "");
                row.createCell(4).setCellValue(event.getVenue() != null ? event.getVenue() : "");
                row.createCell(5).setCellValue(event.getClubName() != null ? event.getClubName() : "");
                row.createCell(6).setCellValue(event.getOrganizerName() != null ? event.getOrganizerName() : "");
                row.createCell(7).setCellValue(event.getParticipantCount() != null ? event.getParticipantCount() : 0);
                row.createCell(8).setCellValue(event.getStatus() != null ? event.getStatus() : "");
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * Export clubs to Excel
     */
    public byte[] exportClubsToExcel(List<Club> clubs) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clubs");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Club Name", "Description", "Advisor Name", "Contact Email"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Club club : clubs) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(club.getId() != null ? club.getId() : 0);
                row.createCell(1).setCellValue(club.getClubName() != null ? club.getClubName() : "");
                row.createCell(2).setCellValue(club.getDescription() != null ? club.getDescription() : "");
                row.createCell(3).setCellValue(club.getAdvisorName() != null ? club.getAdvisorName() : "");
                row.createCell(4).setCellValue(club.getContactEmail() != null ? club.getContactEmail() : "");
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    /**
     * Export attendance to Excel
     */
    public byte[] exportAttendanceToExcel(List<Attendance> attendanceList, Long eventId) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Event ID", "Student Name", "Email", "Roll Number", 
                               "Department", "Check-In Time", "Check-In Method"};

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data style
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MMM-yyyy HH:mm:ss"));

            // Fill data
            int rowNum = 1;
            for (Attendance attendance : attendanceList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(attendance.getId() != null ? attendance.getId() : 0);
                row.createCell(1).setCellValue(attendance.getEventId() != null ? attendance.getEventId() : 0);
                row.createCell(2).setCellValue(attendance.getStudentName() != null ? attendance.getStudentName() : "");
                row.createCell(3).setCellValue(attendance.getStudentEmail() != null ? attendance.getStudentEmail() : "");
                row.createCell(4).setCellValue(attendance.getRollNumber() != null ? attendance.getRollNumber() : "N/A");
                row.createCell(5).setCellValue(attendance.getDepartment() != null ? attendance.getDepartment() : "N/A");
                
                Cell timeCell = row.createCell(6);
                if (attendance.getCheckInTime() != null) {
                    timeCell.setCellValue(attendance.getCheckInTime().format(DATETIME_FORMATTER));
                } else {
                    timeCell.setCellValue("N/A");
                }
                
                row.createCell(7).setCellValue(attendance.getCheckInMethod() != null ? attendance.getCheckInMethod() : "MANUAL");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 20 * 256);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}