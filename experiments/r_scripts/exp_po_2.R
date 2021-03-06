install.packages("PMCMR")
install.packages("FSA")
library("PMCMR", lib.loc="/Library/Frameworks/R.framework/Versions/3.3/Resources/library")
library("FSA", lib.loc="/Library/Frameworks/R.framework/Versions/3.3/Resources/library")

library("fitdistrplus", lib.loc="/Library/Frameworks/R.framework/Versions/3.3/Resources/library")

install.packages("stargazer")
library(stargazer)

library(ggplot2)

setwd("/Users/sic2/git/sos/experiments")
getwd()

# Read the CVS file
# po_a_2_text_100kb_its5
d <- read.csv("remote/po_a_2_text_100kb_its5.tsv", header=TRUE, sep="\t")
d <- d[d$StatsTYPE == 'policies',] # Filter policies measurements
d$Message <- droplevels(d$Message)
d$ContextName <- d$Message
d$ContextName<-factor(d$ContextName, levels=c("no_policies", 
                                              "one_policy_local", "two_policies_local", "three_policies_local",
                                              "four_policies_local", "five_policies_local", "six_policies_local",
                                              "seven_policies_local", "eight_policies_local", "nine_policies_local", "ten_policies_local"
))

d$Measures <- d$User.Measure / 1000000000.0; # Nanoseconds to seconds

################
# Playing with ggplot

dd <- d[d$Subtype == 'policy_apply_dataset',]
dd$Message <- droplevels(dd$Message)
dd <- summarySE(dd, measurevar="Measures", groupvars =c("ContextName", "StatsTYPE"))

ggplot(data=dd, aes(x=dd$ContextName, y=dd$Measures)) + 
  geom_point() +
  geom_errorbar(aes(ymin=dd$Measures-dd$ci, ymax=dd$Measures+dd$ci),width=.2) +
  theme_bw() +
  theme(axis.text.x=element_text(angle=90,hjust=1), 
        axis.text=element_text(size=14),
        axis.title=element_text(size=16,face="bold")) +
  labs(title="Policies per asset....", x="Policy", y="Time (s)")
