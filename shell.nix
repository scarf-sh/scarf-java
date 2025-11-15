{ pkgs ? import <nixpkgs> {} }:

let
  # Prefer Temurin 17 if available in your nixpkgs, otherwise fall back to OpenJDK 17 (or generic OpenJDK).
  jdk = if builtins.hasAttr "temurin-bin-17" pkgs then pkgs."temurin-bin-17"
        else if builtins.hasAttr "jdk17" pkgs then pkgs.jdk17
        else pkgs.openjdk;
in
pkgs.mkShell {
  packages = [ jdk pkgs.maven ];

  shellHook = ''
    export JAVA_HOME=${jdk}
    echo "JAVA_HOME set to ${jdk}"
  '';
}

