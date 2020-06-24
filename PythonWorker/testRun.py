import pycvss.ffmpeg.benchmark as benchmark
import pycvss.ffmpeg.bindings as args
import TaskRequest_pb2
import os

RepDir="./sampleRepo/"
ExpDir="./sampleOutput/"
###############################################################################
def print_sep() -> None:
    """Prints a seperator string of 100 characters"""
    print('-' * 100)


def _format_output(str_: str) -> None:
    """Formats printed output"""
    print(str_)
    print_sep()
###############################################################################
print(os.path.join(RepDir,"1","standardoutput1.ts"))
if __name__ == "__main__":

    
    # Test suite setup
    VideoNum="1"
    SegmentName="standardoutput1.ts"
    tests = [
    #BW test
#    benchmark.BTest(
#            'convert_grayscale',
#            lambda: args.grayscale_conversion_args(
#                os.path.join(RepDir,"1","standardoutput1.ts"),
#                os.path.join(ExpDir,"standardoutput1.ts")),
#            input_file_=os.path.join(RepDir,"1","standardoutput1.ts"),
#            output_file_=os.path.join(ExpDir,"standardoutput1.ts")
#        )
    #try resolution
    benchmark.BTest(
            'Framerate',
            lambda: args.basic_video_convert_args(
                os.path.join(RepDir,VideoNum,SegmentName),
                os.path.join(ExpDir,SegmentName),
                fps_=5,
                ),
            input_file_=os.path.join(RepDir,VideoNum,SegmentName),
            output_file_=os.path.join(ExpDir,SegmentName),
        )
    ]

    #print(tests[0]._args_func)
    # Process each test and capture the results
    results = list()
    for test in tests:
        results.append(test.process())
    print('execution time='+str(results[0][1])) #### this is the execution time
    print('\n[----Benchmark results----]')
    list(map(_format_output, results))
    print()
