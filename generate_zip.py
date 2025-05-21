import os
import zipfile

def create_zip(output_filename, source_dir):
    with zipfile.ZipFile(output_filename, 'w') as zipf:
        for root, dirs, files in os.walk(source_dir):
            for file in files:
                filepath = os.path.join(root, file)
                arcname = os.path.relpath(filepath, source_dir)
                zipf.write(filepath, arcname)

if __name__ == "__main__":
    output_zip = "downloadable_files.zip"
    source_directory = "files"
    
    # Ensure the source directory exists
    if not os.path.exists(source_directory):
        print(f"Source directory '{source_directory}' does not exist.")
    else:
        create_zip(output_zip, source_directory)
        print(f"Created {output_zip} containing files from '{source_directory}'.")
