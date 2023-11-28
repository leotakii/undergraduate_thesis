require(devtools)
#install_version("irace", version = "1.07", repos = "http://cran.us.r-project.org")
library(irace)
irace.cmdline("--hook-run hook-runTested.bat")
