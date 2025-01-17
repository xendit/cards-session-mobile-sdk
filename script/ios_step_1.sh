#!/bin/bash

# Get the script directory and project root directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$( cd "$SCRIPT_DIR/.." && pwd )"

# Configuration
GITHUB_REPO="xendit/cards-session-mobile-sdk"
FRAMEWORK_NAME="cardsSdk"
# Update path to be relative to project root
XCFRAMEWORK_PATH="${PROJECT_ROOT}/cardsSdk/build/cocoapods/publish/release/${FRAMEWORK_NAME}.xcframework"
RELEASE_DIR="${PROJECT_ROOT}/cardsSdk/build/cocoapods/publish/release"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration for Package.swift & .podspec updates
TARGET_NAME="CardsSessionMobileSDK"
PODSPEC_NAME="CardSessionMobileSDK.podspec"


# Function to format version tag to version number
format_version() {
    local version_tag=$1
    echo "${version_tag#v}"
}

# Function to get zip name
get_zip_name() {
    local version_tag=$1
    local version_number=$(format_version "$version_tag")
    echo "${TARGET_NAME}-${version_number}.zip"
}

# Function to print step information
print_step() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

# Function to check if command succeeded
check_status() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ $1 completed successfully${NC}"
    else
        echo -e "${RED}✗ $1 failed${NC}"
        exit 1
    fi
}

# Function to check if file exists
check_file_exists() {
    if [ ! -d "$1" ]; then
        echo -e "${RED}Error: $2 not found at: $1${NC}"
        exit 1
    fi
}

# Function to build XCFramework using Gradle
build_framework() {
    print_step "Building XCFramework"
    cd "$PROJECT_ROOT"
    ./gradlew podPublishReleaseXCFramework
    check_status "Gradle build"
    check_file_exists "$XCFRAMEWORK_PATH" "XCFramework"
}

# Function to create zip of XCFramework
create_zip() {
    print_step "Creating zip file"
    cd "$PROJECT_ROOT"
    (cd "$(dirname "$XCFRAMEWORK_PATH")" && zip -r "$(pwd)/$ZIP_NAME" "$(basename "$XCFRAMEWORK_PATH")")
    check_status "Zip creation"
}

# Function to update Package.swift and podspec
update_package_files() {
    local version_tag=$1
    local filename=$2
    
    print_step "Updating Package.swift and podspec"
    
    # Calculate checksum
    local checksum=$(sha256sum "${RELEASE_DIR}/${filename}" | cut -d ' ' -f 1)
    
    # Create temporary files
    local tmp_swift=$(mktemp)
    local tmp_podspec=$(mktemp)
    local package_swift="${PROJECT_ROOT}/Package.swift"
    local podspec_file="${PROJECT_ROOT}/${PODSPEC_NAME}"
    
    # Update Package.swift
    awk -v target="$TARGET_NAME" -v tag="$version_tag" -v owner="${GITHUB_REPO%/*}" -v repo="${GITHUB_REPO#*/}" \
        -v checksum="$checksum" -v filename="$filename" '
        BEGIN { in_target = 0 }
        {
            if ($0 ~ "name: \"" target "\",$") {
                in_target = 1
                print $0
                next
            }
            
            if (in_target) {
                if ($0 ~ /url:/) {
                    printf "            url: \"https://github.com/%s/%s/releases/download/%s/%s\",\n", owner, repo, tag, filename
                    next
                }
                if ($0 ~ /checksum:/) {
                    printf "            checksum: \"%s\"\n", checksum
                    in_target = 0
                    next
                }
            }
            print $0
        }
    ' "$package_swift" > "$tmp_swift"
    
    # Update podspec
    awk -v tag="$version_tag" -v owner="${GITHUB_REPO%/*}" -v repo="${GITHUB_REPO#*/}" -v filename="$filename" '
        {
            if ($0 ~ /spec\.source.*=.*{.*:http/) {
                printf "    spec.source                   = { :http=> '\''https://github.com/%s/%s/releases/download/%s/%s'\''}\n", owner, repo, tag, filename
            } else if ($0 ~ /spec\.version.*=.*/) {
                printf "    spec.version                  = '\''%s'\''\n", tag
            } else {
                print $0
            }
        }
    ' "$podspec_file" > "$tmp_podspec"
    
    # Replace original files
    mv "$tmp_swift" "$package_swift"
    mv "$tmp_podspec" "$podspec_file"
    
    check_status "Package files update"
}

# Main execution
main() {
    local version_tag=$1
    
    if [ -z "$version_tag" ]; then
        echo -e "${RED}Error: Version tag is required${NC}"
        echo "Usage: $0 <version_tag>"
        echo "Example: $0 v1.0.0"
        exit 1
    fi

    ZIP_NAME=$(get_zip_name "$version_tag")
    
    # Check if required commands exist
    if [ ! -f "${PROJECT_ROOT}/gradlew" ]; then
        echo -e "${RED}Error: gradlew not found in project root directory${NC}" >&2
        exit 1
    fi
    command -v jq >/dev/null 2>&1 || { echo -e "${RED}Error: jq is required but not installed${NC}" >&2; exit 1; }
    
    # Build and create zip
    build_framework
    create_zip
    
    # Update Package.swift and podspec
    update_package_files "$version_tag" "$ZIP_NAME"
    
    echo -e "\n${GREEN}🎉 Process completed successfully!${NC}"
    echo -e "${BLUE}Created zip file at: ${RELEASE_DIR}/${ZIP_NAME}${NC}"
}

# Run the script with version tag argument
main "$1"