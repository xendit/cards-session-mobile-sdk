#!/bin/bash


# Configuration
GITHUB_REPO="xendit/cards-session-mobile-sdk"
GITHUB_TOKEN="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"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if release exists
check_release() {
    local version_tag=$1
    
    print_step "Checking if release $version_tag exists"
    
    response=$(curl -s \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/$GITHUB_REPO/releases/tags/$version_tag")
    
    if [ "$(echo "$response" | jq -r '.message')" = "Not Found" ]; then
        echo -e "${BLUE}Release not found - will create new release${NC}"
        return 1
    else
        release_id=$(echo "$response" | jq -r '.id')
        echo -e "${BLUE}Found existing release with ID: $release_id${NC}"
        return 0
    fi
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

# Function to get existing release upload URL
get_existing_release_url() {
    local version_tag=$1
    
    response=$(curl -s \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/$GITHUB_REPO/releases/tags/$version_tag")
    
    upload_url=$(echo "$response" | jq -r .upload_url | sed 's/{?name,label}//g')
    echo "$upload_url"
}

# Function to delete existing asset if it exists
delete_existing_asset() {
    local version_tag=$1
    
    print_step "Checking for existing assets"
    
    assets_response=$(curl -s \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/$GITHUB_REPO/releases/tags/$version_tag")
    
    asset_id=$(echo "$assets_response" | jq -r ".assets[] | select(.name == \"$ZIP_NAME\") | .id")
    
    if [ ! -z "$asset_id" ] && [ "$asset_id" != "null" ]; then
        echo -e "${BLUE}Deleting existing asset with ID: $asset_id${NC}"
        curl -s -X DELETE \
            -H "Authorization: token $GITHUB_TOKEN" \
            -H "Accept: application/vnd.github.v3+json" \
            "https://api.github.com/repos/$GITHUB_REPO/releases/assets/$asset_id"
        check_status "Asset deletion"
    fi
}

# Function to create a new release
create_release() {
    local version_tag=$1
    
    print_step "Creating new release $version_tag"
    
    response=$(curl -s -X POST \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        "https://api.github.com/repos/$GITHUB_REPO/releases" \
        -d "{
            \"tag_name\": \"$version_tag\",
            \"name\": \"Release $version_tag\",
            \"body\": \"Release $version_tag of $FRAMEWORK_NAME\",
            \"draft\": false,
            \"prerelease\": false
        }")
    
    upload_url=$(echo "$response" | jq -r .upload_url | sed 's/{?name,label}//g')
    
    if [ -z "$upload_url" ] || [ "$upload_url" = "null" ]; then
        echo -e "${RED}Error creating release. Response:${NC}"
        echo "$response"
        exit 1
    fi
    
    echo "$upload_url"
}

# Function to upload asset to release
upload_asset() {
    local upload_url=$1
    local zip_path="$ZIP_PATH"
    
    print_step "Uploading XCFramework"
    
    if [ ! -f "$zip_path" ]; then
        echo -e "${RED}Error: Zip file not found: $zip_path${NC}"
        exit 1
    fi
    
    echo -e "${BLUE}Uploading file: $zip_path${NC}"
    echo -e "${BLUE}File size: $(ls -lh "$zip_path" | awk '{print $5}')${NC}"
    
    cd "$(dirname "$zip_path")"
    
    curl -v \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Content-Type: application/zip" \
        -H "Accept: application/vnd.github.v3+json" \
        --data-binary "@${ZIP_NAME}" \
        "${upload_url}?name=${ZIP_NAME}" 2>&1
    
    check_status "Release upload"
}

# Main execution
main() {
    local version_tag=$1
    local zip_path=$2
    local upload_url
    
    if [ -z "$version_tag" ] || [ -z "$zip_path" ]; then
        echo -e "${RED}Error: Version tag and zip path are required${NC}"
        echo "Usage: $0 <version_tag> <zip_path>"
        echo "Example: $0 v1.0.0 /path/to/framework.zip"
        exit 1
    fi

    ZIP_NAME=$(basename "$zip_path")
    ZIP_PATH="$zip_path"
    
    # Handle GitHub release
    if check_release "$version_tag"; then
        echo -e "${BLUE}Using existing release...${NC}"
        delete_existing_asset "$version_tag"
        upload_url=$(get_existing_release_url "$version_tag")
    else
        echo -e "${BLUE}Creating new release...${NC}"
        upload_url=$(create_release "$version_tag")
    fi
    
    # Upload to GitHub
    upload_asset "$upload_url"
    
    # Cleanup after successful upload
    print_step "Cleaning up"
    rm "$ZIP_PATH" 2>/dev/null
    check_status "Cleanup"
    
    echo -e "\n${GREEN}🎉 Release upload completed successfully!${NC}"
}

# Run the script with version tag and zip path arguments
main "$1" "$2"