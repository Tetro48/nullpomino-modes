# NullpoMino custom modes
Custom modes for Nullpomino.

DummyMode.java shouldn't be modified, since this is the base of most modes.

ChallengerMode.java and other NullpoMino custom modes as a source code is in /src/mu/nu/nullpo/game/subsystem/mode/

Modifying any file outside of mode folder will cause some issues while building, since it affects every mode in build and likely will crash NullpoMino).

Challenger mode description:
Challenging mode. The further you go, it will be harder.

Challenger mode default rulesets: Standard, Standard-Hard128, Classic-Easy-A2, Classic-Easy-B2.

# Mode installation

To install any mode, put a mode (must be .class file) to #nullpomino dir#/bin/mu/nu/nullpo/game/subsystem/mode, then open text editor, and go to /config/list/, and open mode.lst with text editor, and type mu.nu.nullpo.game.subsystem.mode."modename" in any line, no overlaps. "modename" is name of the mode you want to install, e.g. ChallengerMode (shouldn't include .class).
