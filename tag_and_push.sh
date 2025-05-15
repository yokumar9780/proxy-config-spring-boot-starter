#!/bin/bash

# Extract the project version from the pom.xml file (skip the parent version tag)
VERSION=$(sed -n '/<artifactId>proxy-config-spring-boot-starter<\/artifactId>/,/<version>/s:.*<version>\(.*\)</version>.*:\1:p' pom.xml | head -n 1)

# Check if the version was found
if [ -z "$VERSION" ]; then
  echo "Project version not found in pom.xml"
  exit 1
fi

# Increment the minor version by 1
IFS='.' read -r MAJOR MINOR PATCH <<< "$VERSION"
MINOR=$((MINOR + 1))
NEW_VERSION="$MAJOR.$MINOR.$PATCH"

# Run the git commands
echo "Tagging version v$NEW_VERSION and pushing to origin..."
#git tag "v$NEW_VERSION"
#git push origin "v$NEW_VERSION"

echo "Done."