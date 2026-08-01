# Safe SDD installer

`installer.py` requires an explicit payload and target when used from an installed project:

```bash
python3 tools/sdd-installer/installer.py --payload /controlled/framework/payload --target /project --plan
python3 tools/sdd-installer/installer.py --payload /controlled/framework/payload --target /project --apply
```

It never overwrites an untracked or user-modified file. Apply writes a manifest and recoverable backup; rollback refuses post-install drift unless an authorized operator explicitly uses `--force`.
