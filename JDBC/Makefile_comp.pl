#!/usr/bin/perl
#
# A script to allow Bash to complete make targets.
#
# To install for Bash 2.0 or better, add the following to ~/.bashrc:
#     $ complete -C ./Makefile_comp.pl make gmake
#

my $word = $ARGV[ 1 ];
getResult( getCompletions( $word ) );
exit( 0 );

sub getResult
{
    for (@_)
	{
        print "$_\n";
    }
}
sub getCompletions
{
    my ($word) = @_;
	my @completions =
		qw(all test help clean classes compile build librom rom list ignored testall
			xstestall xstestall-tearing xs-tearing
			TEST= XSHOST= DBMSHOST= RUNNER= PAUSE= DELETE=);
    grep( /^\Q$word\E/, @completions );
}
