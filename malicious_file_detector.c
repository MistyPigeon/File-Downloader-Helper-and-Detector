#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_LINE_LENGTH 1024
#define MAX_SIGNATURES 10

// Array to store known malicious patterns
const char *malicious_signatures[MAX_SIGNATURES] = {
    "eval(",       // Often used in malicious scripts
    "system(",     // Executes shell commands
    "exec(",       // Executes external commands
    "rm -rf",      // Deletes files/directories
    "wget",        // Downloads files
    "curl",        // Fetches files from a URL
    "base64_decode(", // Decoding potentially malicious data
    "<script>",    // Injected HTML/JS scripts
    "<?php",       // PHP code injection
    "DROP TABLE"   // SQL injection
};

// Function to scan a file for malicious patterns
int scan_file(const char *filename) {
    FILE *file = fopen(filename, "r");
    if (!file) {
        perror("Error opening file");
        return -1;
    }

    char line[MAX_LINE_LENGTH];
    int line_number = 0;
    int found_malicious = 0;

    printf("Scanning file: %s\n", filename);

    // Read the file line by line
    while (fgets(line, MAX_LINE_LENGTH, file)) {
        line_number++;
        for (int i = 0; i < MAX_SIGNATURES; i++) {
            if (strstr(line, malicious_signatures[i])) {
                printf("Malicious pattern found: '%s' on line %d\n", malicious_signatures[i], line_number);
                found_malicious = 1;
            }
        }
    }

    fclose(file);

    if (found_malicious) {
        printf("Malicious content detected in the file.\n");
    } else {
        printf("No malicious content detected in the file.\n");
    }

    return found_malicious;
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        printf("Usage: %s <file_to_scan>\n", argv[0]);
        return 1;
    }

    const char *file_to_scan = argv[1];
    int result = scan_file(file_to_scan);

    if (result == -1) {
        printf("An error occurred while scanning the file.\n");
        return 1;
    }

    if (result == 1) {
        printf("Warning: The file contains malicious content!\n");
    } else {
        printf("The file is clean.\n");
    }

    return 0;
}
