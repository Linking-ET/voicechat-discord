with import <nixpkgs> { };
mkShell {
  buildInputs = [
    jq

    libopus
    pkg-config
  ] ++ lib.optional stdenv.hostPlatform.isDarwin darwin.libiconv;
}
