# Beta 0.6.7


## Additions
- Table creation wizard. (#907)
- Add support for multiple citations in one command. (#921)
- Project-specific setting to change compiler compatibility. (#891)
- Added an option to the run config to always compile twice. (#908)
- Added support for automatically running makeindex in auxiliary directories. (#924)
- Added support for Okular. (#1064)
- Added inspection that checks whether the \addbibresource has a file extension. (#956)
- Added support for including packages in package or documentclass options. (#911, #949)
- Added menu entry to clear all generated files. (#971)
- Added continuous preview of math and TikZ pictures. (#923)
- Specify custom commands that define a label. (#815)

## Changes
- Performance improvements. (#932, #944)
- Inspection for normal space after abbreviation will not be triggered in comments. (#983)
- The 'file not found' inspection quickfix now also uses the file creation dialog. (#1004)
- Internal code improvements.

## Bug fixes
- Renamed labels will now be renamed in all files. (#950)
- Fixed not being able to disable bibtex. (#945)
- Fixed package dependencies being inserted in the wrong file. (#942)
- Support compilation of non-project files. (#987)
- Fixed crash when providing optional parameters to a \ref command as comma separated list. (#980)
- Fixed missing autocompletion when a file name is equal to a folder name. (#937)
- Fixed crashes. (#909, #948, #963)

A detailed overview of the changes can be found on the [milestone page](https://github.com/Hannah-Sten/TeXiFy-IDEA/milestone/16).