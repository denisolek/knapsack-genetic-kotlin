import java.util.*

fun main(args: Array<String>) {
    val kp = KnapsackProblem()

    kp.makePopulation()

    println("=========================================================")
    println("INITIAL GENERATION:")
    printPopulation(kp)
    kp.evalPopulation(kp.population)
    printFitness(kp)
    kp.getSummary(0)
    kp.makeGenerations()
}

private fun printPopulation(kp: KnapsackProblem) {
    println("\nPopulation:")
    kp.population.forEachIndexed { index, elem -> println("${index + 1} - $elem") }
}

private fun printFitness(kp: KnapsackProblem) {
    println("\nFitness:")
    kp.fitness.forEachIndexed { index, elem -> println("${index + 1} - $elem") }
}

class KnapsackProblem {
    /**
     * Setup
     */
    private val knapsackCapacity: Double = 100.0
    private var populationSize: Int = 10
    private var maxGenerations: Int = 70
    private val crossoverProbability: Double = 0.5
    private val mutationProbability: Double = 0.03

    /**
     * Init variables
     */
    val population: MutableList<String> = mutableListOf()
    private var totalGenerationFitness: Double = 0.0
    var fitness: MutableList<Double> = mutableListOf()
    private val bestGenerationSolution: MutableList<String> = mutableListOf()
    private val bestGenerationFitness: MutableList<Double> = mutableListOf()
    private val meanGenerationFitness: MutableList<Double> = mutableListOf()
    private var mutation: Boolean = false
    private var crossoverCount: Int = 0
    private var cloneCount: Int = 0
    private var generationCounter: Int = 1
    private val breedPopulation: MutableList<String> = mutableListOf()

    private val items = listOf(
        Item(24.0, 12.0),
        Item(13.0, 7.0),
        Item(23.0, 11.0),
        Item(15.0, 8.0),
        Item(14.0, 9.0),
        Item(3.0, 6.0),
        Item(2.0, 5.0),
        Item(7.0, 14.0),
        Item(32.0, 12.0),
        Item(14.0, 91.0),
        Item(51.0, 23.0),
        Item(23.0, 7.0),
        Item(4.0, 5.0),
        Item(65.0, 30.0),
        Item(3.0, 4.0)
    )

    /**
     * Filling population with random genes
     */
    fun makePopulation() {
        for (i in 0 until populationSize) {
            population.add(generateGene())
        }
    }

    /**
     * Generates gene - random String of 1s and 0s
     */
    private fun generateGene(): String {
        var gene = ""
        for (i in 0 until items.size) {
            gene += (0..2).random().toString()
        }
        return gene
    }

    /**
     * Evaluates population fitness
     */
    fun evalPopulation(population: MutableList<String>) {
        totalGenerationFitness = 0.0
        for (i in 0 until populationSize) {
            val tmpFitness = evalGene(population[i])
            fitness.add(tmpFitness)
            totalGenerationFitness += tmpFitness
        }
    }

    /**
     * Evaluates a single gene's fitness, by calculating the total_weight
     * of items selected by the gene
     * @return double - gene's total fitness value
     */
    fun evalGene(gene: String): Double {
        var totalWeight = 0.0
        var totalValue = 0.0
        var fitnessValue = 0.0
        var chromosome = '0'

        // totalWeight of items in this gene
        items.forEachIndexed { index, item ->
            chromosome = gene[index]
            if (chromosome == '1') {
                totalWeight += item.weight
                totalValue += item.value
            }
        }

        // if gene items doesn't fit in knapsack fitnessValue is by default 0.0
        if (totalWeight < knapsackCapacity) {
            fitnessValue = totalValue
        }
        return fitnessValue
    }

    /**
     * Gets best solution in population
     */
    private val bestSolution: Int
        get() {
            var bestPosition = 0
            var currentFitness = 0.0
            var bestFitness = 0.0
            for (i in 0 until populationSize) {
                currentFitness = evalGene(population[i])
                if (currentFitness > bestFitness) {
                    bestFitness = currentFitness
                    bestPosition = i
                }
            }
            return bestPosition
        }

    /**
     * Gets mean fitness of generation
     */
    private val meanFitness: Double
        get() {
            var totalFitness = 0.0
            var meanFitness = 0.0
            for (i in 0 until populationSize) {
                totalFitness += fitness[i]
            }
            meanFitness = totalFitness / populationSize
            return meanFitness
        }

    fun getSummary(generationIndex: Int) {
        bestGenerationSolution.add(population[bestSolution])
        bestGenerationFitness.add(evalGene(population[bestSolution]))
        meanGenerationFitness.add(meanFitness)
        println("\n-----------------------------------")
        println("| Best solution: ${bestGenerationSolution[generationIndex]}")
        println("| Best fitness: ${bestGenerationFitness[generationIndex]}")
        println("| Worst fitness: ${meanGenerationFitness[generationIndex]}")
        println("-----------------------------------")
        println("| Crossover:  $crossoverCount times")
        println("| Cloning:  $cloneCount times")
        when {
            mutation -> println("| Mutation did occur")
            else -> println("| Mutation did not occur")
        }
        println("-----------------------------------")
    }

    /**
     * Makes further generations beyond first, if necessary
     */
    fun makeGenerations() {

        for (i in 1 until maxGenerations) {

            if (checkForStopCriteria(i)) break

            resetCounters()

            // Breed population
            for (j in 0 until populationSize / 2) {
                this.breedPopulation()
            }

            // Clear fitness values of previous generation
            fitness.clear()

            // Evaluate fitness of breed population members
            evalPopulation(breedPopulation)

            // Copy breed_population to population
            for (k in 0 until populationSize) {
                population[k] = breedPopulation[k]
            }

            println("=========================================================")
            println("\nGENERATION ${(i + 1)}")

            printPopulation(this)
            printFitness(this)
            breedPopulation.clear()

            getSummary(i)
        }
    }

    private fun resetCounters() {
        crossoverCount = 0
        cloneCount = 0
        mutation = false
    }

    private fun checkForStopCriteria(i: Int): Boolean {
        if (maxGenerations > 4 && i > 4) {

            // Previous 3 generational fitness values
            val a = meanGenerationFitness[i - 1]
            val b = meanGenerationFitness[i - 2]
            val c = meanGenerationFitness[i - 3]

            if (a == b && b == c) {
                println("\nStop criterion met")
                maxGenerations = i
                return true
            }
        }
        return false
    }

    /**
     * Breeds current population to create a new generation's population
     */
    private fun breedPopulation() {
        val geneOne: Int = selectGene()
        val geneTwo: Int = selectGene()
        generationCounter += 1

        // If population size is odd clone best solution from previous generation
        if (populationSize % 2 == 1) {
            breedPopulation.add(bestGenerationSolution[generationCounter - 1])
        }

        // Crossover or cloning
        crossoverGenes(geneOne, geneTwo)
    }

    /**
     * Selects a gene for breeding
     */
    private fun selectGene(): Int {
        var rand = Math.random() * totalGenerationFitness
        for (i in 0 until populationSize) {
            if (rand <= fitness[i]) {
                return i
            }
            rand -= fitness[i]
        }
        return 0
    }

    /**
     * Performs either crossover or cloning
     */
    private fun crossoverGenes(geneOne: Int, geneTwo: Int) {
        var newGeneOne: String = ""
        var newGeneTwo: String = ""

        val crossoverRand = Math.random()
        if (crossoverRand <= crossoverProbability) {
            crossoverCount += 1
            val generator = Random()
            val crossingPoint = generator.nextInt(items.size) + 1

            // Crossing genes at randomly chosen spot
            newGeneOne = population[geneOne].substring(0, crossingPoint) + population[geneTwo].substring(crossingPoint)
            newGeneTwo = population[geneTwo].substring(0, crossingPoint) + population[geneOne].substring(crossingPoint)

            // Add new genes to breed_population
            breedPopulation.add(newGeneOne)
            breedPopulation.add(newGeneTwo)
        } else {
            cloneCount += 1
            breedPopulation.add(population[geneOne])
            breedPopulation.add(population[geneTwo])
        }
        mutateGene()
    }

    /**
     * Performs mutation, if necessary
     */
    private fun mutateGene() {

        val mutationRand = Math.random()
        if (mutationRand <= mutationProbability) {
            mutation = true
            var mutatedGene: String
            var newMutatedGene: String
            val generator = Random()
            var mutationPoint = 0
            val whichGene = Math.random() * 100

            if (whichGene <= 50) {
                mutatedGene = breedPopulation[breedPopulation.size - 1]
                mutationPoint = generator.nextInt(items.size)
                if (mutatedGene.substring(mutationPoint, mutationPoint + 1) == "1") {
                    newMutatedGene = mutatedGene.substring(0, mutationPoint) + "0" +
                            mutatedGene.substring(mutationPoint)
                    breedPopulation[breedPopulation.size - 1] = newMutatedGene
                }
                if (mutatedGene.substring(mutationPoint, mutationPoint + 1) == "0") {
                    newMutatedGene = mutatedGene.substring(0, mutationPoint) + "1" +
                            mutatedGene.substring(mutationPoint)
                    breedPopulation[breedPopulation.size - 1] = newMutatedGene
                }
            }
            if (whichGene > 50) {
                mutatedGene = breedPopulation[breedPopulation.size - 2]
                mutationPoint = generator.nextInt(items.size)
                if (mutatedGene.substring(mutationPoint, mutationPoint + 1) == "1") {
                    newMutatedGene = mutatedGene.substring(0, mutationPoint) + "0" +
                            mutatedGene.substring(mutationPoint)
                    breedPopulation[breedPopulation.size - 1] = newMutatedGene
                }
                if (mutatedGene.substring(mutationPoint, mutationPoint + 1) == "0") {
                    newMutatedGene = mutatedGene.substring(0, mutationPoint) + "1" +
                            mutatedGene.substring(mutationPoint)
                    breedPopulation[breedPopulation.size - 2] = newMutatedGene
                }
            }
        }
    }
}
