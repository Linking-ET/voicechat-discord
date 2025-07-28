#!/bin/sh

set -e

cargo build

./copy_natives.sh
