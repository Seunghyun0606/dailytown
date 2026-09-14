# FAIL_MORU_02_SEMANTIC_FAMILY_V1

- runner commit: 66b198e95c4165e46819df6129ba44fb042494ea
- exit code: 1
- rejected production binaries persisted: false

```text
Traceback (most recent call last):
  File "/home/runner/work/dailytown/dailytown/scripts/moru_moru02_semantic_family_v1_once.py", line 459, in <module>
    if __name__ == "__main__": main()
                               ^^^^^^
  File "/home/runner/work/dailytown/dailytown/scripts/moru_moru02_semantic_family_v1_once.py", line 386, in main
    generated=derive_usage(master,usage); persisted=Image.open(USAGE_ROOT/"candidate"/fn).convert("RGBA")
              ^^^^^^^^^^^^^^^^^^^^^^^^^^
  File "/home/runner/work/dailytown/dailytown/scripts/moru_moru02_semantic_family_v1_once.py", line 180, in derive_usage
    return fit_usage(src, sub, *specs[usage])
           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  File "/home/runner/work/dailytown/dailytown/scripts/moru_moru02_semantic_family_v1_once.py", line 154, in fit_usage
    a, b, c, d = fr
    ^^^^^^^^^^
ValueError: not enough values to unpack (expected 4, got 2)
```
